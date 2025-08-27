package com.eatcloud.storeservice.domain.menuai.service;

import com.eatcloud.storeservice.domain.menuai.dto.MenuSimilarityResult;
import com.eatcloud.storeservice.domain.menuai.entity.MenuVector;
import com.eatcloud.storeservice.domain.menuai.repository.MenuVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuSimilarityService {

	private final MenuVectorRepository menuVectorRepository;

	/**
	 * 특정 메뉴와 유사한 메뉴들을 찾아서 반환
	 */
	public List<MenuSimilarityResult> findSimilarMenus(String menuName, int limit) {
		try {
			log.info("=== findSimilarMenus 시작 ===");
			log.info("검색할 메뉴명: '{}'", menuName);
			log.info("요청된 개수: {}", limit);
			
			// 기준 메뉴 벡터 조회
			Optional<MenuVector> baseVector = menuVectorRepository.findByMenuName(menuName);
			if (baseVector.isEmpty()) {
				log.warn("기준 메뉴 벡터를 찾을 수 없습니다: '{}'", menuName);
				log.info("데이터베이스에 저장된 메뉴명들:");
				List<MenuVector> allVectors = menuVectorRepository.findAll();
				allVectors.forEach(vector -> log.info("  - '{}'", vector.getMenuName()));
				return Collections.emptyList();
			}

			log.info("기준 메뉴 벡터 찾음: {}", baseVector.get().getMenuName());
			
			// 기준 메뉴의 TF-IDF 벡터
			Map<String, Double> baseTfidfVector = baseVector.get().getTfidfVector();
			log.info("기준 메뉴 벡터 내용: {}", baseTfidfVector);
			
			// 모든 메뉴 벡터 조회
			List<MenuVector> allVectors = menuVectorRepository.findAll();
			log.info("전체 메뉴 벡터 개수: {}", allVectors.size());
			
			// 유사도 계산 및 정렬
			List<MenuSimilarityResult> results = allVectors.stream()
					.filter(vector -> !vector.getMenuName().equals(menuName)) // 자기 자신 제외
					.map(vector -> {
						try {
							Map<String, Double> compareVector = vector.getTfidfVector();
							double similarity = calculateEnhancedSimilarity(baseTfidfVector, compareVector);
							
							log.debug("메뉴 '{}'와의 유사도: {}", vector.getMenuName(), similarity);
							
							return MenuSimilarityResult.builder()
									.id(vector.getId())
									.menuName(vector.getMenuName())
									.similarity(similarity)
									.build();
						} catch (Exception e) {
							log.error("벡터 유사도 계산 실패: {}", vector.getMenuName(), e);
							return null;
						}
					})
					.filter(Objects::nonNull)
					.sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity())) // 유사도 높은 순
					.limit(limit)
					.collect(Collectors.toList());

			log.info("=== findSimilarMenus 완료 ===");
			log.info("메뉴 '{}'에 대한 유사 메뉴 {}개를 찾았습니다.", menuName, results.size());
			if (!results.isEmpty()) {
				log.info("상위 3개 결과:");
				results.stream().limit(3).forEach(result -> 
					log.info("  - {} (유사도: {})", result.getMenuName(), result.getSimilarity()));
			}
			return results;

		} catch (Exception e) {
			log.error("=== findSimilarMenus 실패 ===");
			log.error("메뉴명: '{}'", menuName);
			log.error("에러 상세: ", e);
			return Collections.emptyList();
		}
	}

	/**
	 * 검색 쿼리와 유사한 메뉴들을 찾아서 반환
	 */
	public List<MenuSimilarityResult> searchMenusByQuery(String query, int limit) {
		try {
			// 쿼리 벡터 생성
			Map<String, Double> queryVector = createQueryVector(query);
			
			// 모든 메뉴 벡터 조회
			List<MenuVector> allVectors = menuVectorRepository.findAll();
			
			// 유사도 계산 및 정렬
			List<MenuSimilarityResult> results = allVectors.stream()
					.map(vector -> {
						Map<String, Double> menuVector = vector.getTfidfVector();
						double similarity = calculateEnhancedSimilarity(queryVector, menuVector);
						
						return MenuSimilarityResult.builder()
								.id(vector.getId())
								.menuName(vector.getMenuName())
								.similarity(similarity)
								.build();
					})
					.sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity())) // 유사도 높은 순
					.limit(limit)
					.collect(Collectors.toList());

			log.info("쿼리 '{}'에 대한 유사 메뉴 {}개를 찾았습니다.", query, results.size());
			return results;

		} catch (Exception e) {
			log.error("쿼리 검색 실패: {}", query, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 쿼리 벡터 생성
	 */
	private Map<String, Double> createQueryVector(String query) {
		// 쿼리를 단어로 분리하고 간단한 TF 계산
		String[] words = query.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
				.trim()
				.split("\\s+");
		
		Map<String, Double> queryVector = new HashMap<>();
		for (String word : words) {
			if (word.length() > 1) {
				queryVector.put(word, queryVector.getOrDefault(word, 0.0) + 1.0);
			}
		}
		
		return queryVector;
	}

	/**
	 * 개선된 유사도 계산 (RealVector 기반)
	 */
	private double calculateEnhancedSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
		// 1. RealVector 기반 코사인 유사도
		double cosineSimilarity = calculateCosineSimilarityWithRealVector(vector1, vector2);
		
		// 2. 부분 단어 매칭 점수
		double partialMatchScore = calculatePartialMatchScore(vector1, vector2);
		
		// 가중 평균으로 최종 유사도 계산
		double finalSimilarity = (cosineSimilarity * 0.6) + (partialMatchScore * 0.4);
		
		return Math.round(finalSimilarity * 1000.0) / 1000.0; // 소수점 3자리
	}

	/**
	 * RealVector를 사용한 코사인 유사도 계산
	 */
	private double calculateCosineSimilarityWithRealVector(Map<String, Double> vector1, Map<String, Double> vector2) {
		if (vector1.isEmpty() || vector2.isEmpty()) {
			return 0.0;
		}

		try {
			// 모든 고유한 단어들 수집
			Set<String> allTerms = new HashSet<>();
			allTerms.addAll(vector1.keySet());
			allTerms.addAll(vector2.keySet());

			// RealVector 생성
			RealVector v1 = new ArrayRealVector(allTerms.size());
			RealVector v2 = new ArrayRealVector(allTerms.size());

			// 벡터 값 설정
			int index = 0;
			for (String term : allTerms) {
				double val1 = vector1.getOrDefault(term, 0.0);
				double val2 = vector2.getOrDefault(term, 0.0);
				
				v1.setEntry(index, val1);
				v2.setEntry(index, val2);
				index++;
			}

			// 코사인 유사도 계산
			double dotProduct = v1.dotProduct(v2);
			double norm1 = v1.getNorm();
			double norm2 = v2.getNorm();

			if (norm1 == 0.0 || norm2 == 0.0) {
				return 0.0;
			}

			return dotProduct / (norm1 * norm2);
			
		} catch (Exception e) {
			log.warn("RealVector 코사인 유사도 계산 실패, 기본 방식으로 대체", e);
			return calculateCosineSimilarity(vector1, vector2);
		}
	}

	/**
	 * 기존 코사인 유사도 계산 (백업용)
	 */
	private double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
		if (vector1.isEmpty() || vector2.isEmpty()) {
			return 0.0;
		}

		// 모든 고유한 단어들 수집
		Set<String> allTerms = new HashSet<>();
		allTerms.addAll(vector1.keySet());
		allTerms.addAll(vector2.keySet());

		double dotProduct = 0.0;
		double norm1 = 0.0;
		double norm2 = 0.0;

		for (String term : allTerms) {
			double val1 = vector1.getOrDefault(term, 0.0);
			double val2 = vector2.getOrDefault(term, 0.0);
			
			dotProduct += val1 * val2;
			norm1 += val1 * val1;
			norm2 += val2 * val2;
		}

		if (norm1 == 0.0 || norm2 == 0.0) {
			return 0.0;
		}

		return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
	}

	/**
	 * 부분 단어 매칭 점수 계산
	 */
	private double calculatePartialMatchScore(Map<String, Double> vector1, Map<String, Double> vector2) {
		double totalScore = 0.0;
		int matchCount = 0;
		
		for (String term1 : vector1.keySet()) {
			for (String term2 : vector2.keySet()) {
				double matchScore = calculateTermSimilarity(term1, term2);
				if (matchScore > 0.0) {
					totalScore += matchScore;
					matchCount++;
				}
			}
		}
		
		return matchCount > 0 ? totalScore / matchCount : 0.0;
	}

	/**
	 * 단어 간 유사도 계산 (개선된 버전)
	 */
	private double calculateTermSimilarity(String term1, String term2) {
		try {
			// null 체크
			if (term1 == null || term2 == null) {
				return 0.0;
			}
			
			// 완전 일치
			if (term1.equals(term2)) {
				return 1.0;
			}
			
			// 부분 포함 (긴 단어가 짧은 단어를 포함)
			if (term1.length() > term2.length()) {
				if (term1.contains(term2) && term2.length() > 1) {
					return 0.8;
				}
			} else {
				if (term2.contains(term1) && term1.length() > 1) {
					return 0.8;
				}
			}
			
			// 공통 문자 수 기반 유사도 (한글 지원)
			int commonChars = countCommonCharacters(term1, term2);
			int totalChars = Math.max(term1.length(), term2.length());
			
			if (totalChars > 0 && commonChars > 0) {
				double charSimilarity = (double) commonChars / totalChars;
				// 공통 문자가 50% 이상일 때만 유사도 점수 부여
				return charSimilarity > 0.5 ? charSimilarity * 0.6 : 0.0;
			}
			
			return 0.0;
			
		} catch (Exception e) {
			log.warn("단어 유사도 계산 중 오류 발생: '{}' vs '{}'", term1, term2, e);
			return 0.0;
		}
	}

	/**
	 * 공통 문자 수 계산 (한글 지원)
	 */
	private int countCommonCharacters(String str1, String str2) {
		Map<Character, Integer> charCount1 = new HashMap<>();
		Map<Character, Integer> charCount2 = new HashMap<>();
		
		// 첫 번째 문자열의 문자 빈도 계산
		for (char c : str1.toCharArray()) {
			charCount1.put(c, charCount1.getOrDefault(c, 0) + 1);
		}
		
		// 두 번째 문자열의 문자 빈도 계산
		for (char c : str2.toCharArray()) {
			charCount2.put(c, charCount2.getOrDefault(c, 0) + 1);
		}
		
		// 공통 문자 수 계산
		int commonCount = 0;
		for (char c : charCount1.keySet()) {
			if (charCount2.containsKey(c)) {
				commonCount += Math.min(charCount1.get(c), charCount2.get(c));
			}
		}
		
		return commonCount;
	}
}
