package com.eatcloud.storeservice.domain.menuai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eatcloud.storeservice.domain.menu.entity.Menu;
import com.eatcloud.storeservice.domain.menuai.entity.MenuVector;
import com.eatcloud.storeservice.domain.menuai.repository.MenuVectorRepository;


import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TFIDFService {

	@Autowired
	private MenuVectorRepository menuVectorRepository;

	/**
	 * 메뉴의 TF-IDF 벡터를 생성하고 저장
	 */
	public void generateAndSaveMenuVector(Menu menu) {
		try {
			String menuName = menu.getMenuName();
			
			// 기존 벡터가 있는지 확인
			Optional<MenuVector> existingVector = menuVectorRepository.findByMenuName(menuName);
			if (existingVector.isPresent()) {
				log.info("메뉴 벡터가 이미 존재합니다: {}", menuName);
				return;
			}

			// TF-IDF 벡터 생성
			Map<String, Double> tfidfVector = calculateTFIDFVector(menuName);
			
			// MenuVector 엔티티 생성 및 저장
			MenuVector menuVector = MenuVector.builder()
					.menuName(menuName)
					.tfidfVector(tfidfVector)
					.build();
			
			menuVectorRepository.save(menuVector);
			log.info("메뉴 벡터 생성 완료: {} - {}", menuName, tfidfVector);
			
		} catch (Exception e) {
			log.error("메뉴 벡터 생성 실패: {}", menu.getMenuName(), e);
		}
	}

	// 메뉴 이름만으로 TF-IDF 벡터 계산
	private Map<String, Double> calculateTFIDFVector(String menuName) {
		// 1. 메뉴 이름의 TF 계산
		Map<String, Integer> termFrequency = calculateTermFrequency(menuName);

		// 2. 전체 메뉴에서의 IDF 계산
		List<MenuVector> allVectors = menuVectorRepository.findAllVectors();
		Map<String, Double> inverseDocumentFrequency = calculateIDF(allVectors, termFrequency.keySet());

		// 3. TF-IDF 계산
		Map<String, Double> tfidfVector = new HashMap<>();
		for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
			String term = entry.getKey();
			int tf = entry.getValue();
			double idf = inverseDocumentFrequency.getOrDefault(term, 0.0);
			double tfidf = tf * idf;

			if (tfidf > 0.01) { // 임계값 이상만 저장
				tfidfVector.put(term, Math.round(tfidf * 100.0) / 100.0); // 소수점 2자리
			}
		}

		return tfidfVector;
	}

	// 메뉴 삭제 시 벡터도 함께 삭제
	public void deleteMenuVector(String menuName) {
		try {
			Optional<MenuVector> existingVector = menuVectorRepository.findByMenuName(menuName);
			if (existingVector.isPresent()) {
				menuVectorRepository.deleteByMenuName(menuName);
				log.info("메뉴 벡터 삭제 완료: {}", menuName);
			} else {
				log.info("삭제할 메뉴 벡터가 존재하지 않습니다: {}", menuName);
			}
		} catch (Exception e) {
			log.error("메뉴 벡터 삭제 실패: {}", menuName, e);
		}
	}

	// 단어 빈도 계산 (메뉴 이름만)
	private Map<String, Integer> calculateTermFrequency(String menuName) {
		Map<String, Integer> frequency = new HashMap<>();

		// 메뉴 이름을 공백 기준으로 분리
		String[] words = menuName.split("\\s+");

		for (String word : words) {
			if (word.length() > 1) { // 1글자 이하 제외
				frequency.put(word, frequency.getOrDefault(word, 0) + 1);
			}
		}

		return frequency;
	}

	// IDF 계산 (간소화)
	private Map<String, Double> calculateIDF(List<MenuVector> allVectors, Set<String> terms) {
		Map<String, Double> idf = new HashMap<>();
		int totalDocuments = allVectors.size() + 1; // 현재 메뉴 포함

		for (String term : terms) {
			int documentsWithTerm = 1; // 현재 메뉴는 항상 포함

			for (MenuVector vector : allVectors) {
				if (vector.getTfidfVector() != null &&
					vector.getTfidfVector().containsKey(term)) {
					documentsWithTerm++;
				}
			}

			idf.put(term, Math.log((double) totalDocuments / documentsWithTerm));
		}

		return idf;
	}
}
