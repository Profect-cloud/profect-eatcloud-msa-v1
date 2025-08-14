package com.eatcloud.adminservice.global.queryDSL;

import com.querydsl.core.types.dsl.BooleanExpression;
import profect.eatcloud.domain.admin.entity.*;
import profect.eatcloud.domain.customer.entity.*;
import profect.eatcloud.domain.manager.entity.*;
import profect.eatcloud.domain.order.entity.*;
import profect.eatcloud.domain.payment.entity.*;
import profect.eatcloud.domain.store.entity.*;


public class SoftDeletePredicates {

    public static BooleanExpression customerActive() {
        return QCustomer.customer.timeData.deletedAt.isNull();
    }

    public static BooleanExpression storeActive() {
        return QStore.store.timeData.deletedAt.isNull();
    }

    public static BooleanExpression menuActive() {
        return QMenu.menu.timeData.deletedAt.isNull();
    }

    public static BooleanExpression orderActive() {
        return QOrder.order.timeData.deletedAt.isNull();
    }

    public static BooleanExpression reviewActive() {
        return QReview.review.timeData.deletedAt.isNull();
    }

    public static BooleanExpression paymentActive() {
        return QPayment.payment.timeData.deletedAt.isNull();
    }

    public static BooleanExpression managerActive() {
        return QManager.manager.timeData.deletedAt.isNull();
    }

    public static BooleanExpression adminActive() {
        return QAdmin.admin.timeData.deletedAt.isNull();
    }

    public static BooleanExpression dailyStoreSalesActive() {
        return QDailyStoreSales.dailyStoreSales.timeData.deletedAt.isNull();
    }

    public static BooleanExpression dailyMenuSalesActive() {
        return QDailyMenuSales.dailyMenuSales.timeData.deletedAt.isNull();
    }

    public static BooleanExpression salesWithStoreActive() {
        return dailyStoreSalesActive().and(storeActive());
    }

    public static BooleanExpression menuSalesWithStoreAndMenuActive() {
        return dailyMenuSalesActive()
                .and(storeActive())
                .and(menuActive());
    }

    public static BooleanExpression orderWithCustomerActive() {
        return orderActive().and(customerActive());
    }

    public static BooleanExpression orderWithStoreActive() {
        return orderActive().and(storeActive());
    }

    public static BooleanExpression orderFullyActive() {
        return orderActive()
                .and(customerActive())
                .and(storeActive());
    }
}