package com.moin.project.domain.transfer.repository

import com.moin.project.domain.transfer.QQuotation
import com.moin.project.domain.transfer.dao.TodayTransferDao
import com.moin.project.domain.transfer.dao.TransferHistoryDao
import com.moin.project.domain.user.QUser
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


@Repository
class QuotationCustomRepositoryImpl(private val queryFactory: JPAQueryFactory) : QuotationCustomRepository {
    private val quotation: QQuotation = QQuotation.quotation
    private val user: QUser = QUser.user

    override fun searchUserIdTypeAndTodayUsdAmount(userSn: Long): TodayTransferDao {
        val startDate: LocalDateTime = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay()
        val endDate: LocalDateTime = LocalDate.now(ZoneId.of("Asia/Seoul")).atTime(LocalTime.MAX)

        return queryFactory
            .select(
                Projections.fields(
                    TodayTransferDao::class.java,
                    user.userId,
                    user.name,
                    user.idType,
                    quotation.usdAmount.count().`as`("todayTransferCount"),
                    quotation.usdAmount.sum().coalesce(BigDecimal(0)).`as`("todayTransferUsdAmount"),
                )
            )
            .from(user)
            .leftJoin(quotation).on(user.eq(quotation.user),
                quotation.requestedDate.between(startDate, endDate)
            )
            .where(user.sn.eq(userSn))
            .groupBy(user.idType)
            .fetchOne()!!
    }

    override fun searchTransferHistory(userSn: Long): List<TransferHistoryDao>? {
        return queryFactory
            .select(
                Projections.fields(
                    TransferHistoryDao::class.java,
                    quotation.sourceAmount.`as`("_sourceAmount"),
                    quotation.fee.`as`("_fee"),
                    quotation.usdExchangeRate,
                    quotation.usdAmount,
                    quotation.targetCurrency,
                    quotation.exchangeRate,
                    quotation.targetAmount.`as`("_targetAmount"),
                    quotation.requestedDate
                )
            )
            .from(quotation)
            .join(user).on(quotation.user.eq(user))
            .where(quotation.user.sn.eq(userSn),
                quotation.transferRequest.eq(true))
            .orderBy(quotation.requestedDate.desc())
            .fetch()

    }
}
