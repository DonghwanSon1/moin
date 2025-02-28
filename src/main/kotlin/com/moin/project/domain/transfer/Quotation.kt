package com.moin.project.domain.transfer

import com.moin.project.domain.transfer.enums.CurrencyInfo
import com.moin.project.domain.user.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
@Table(name = "quotation",
    indexes = [
        Index(name = "idx_user_sn_transfer_request", columnList = "user_sn, transfer_request"),
        Index(name = "idx_user_sn_requested_date", columnList = "user_sn, requested_date")
    ])
class Quotation(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  val id: Long? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_sn", nullable = false)
  val user: User,

  @Column(name = "source_amount", nullable = false)
  val sourceAmount: BigDecimal,

  @Column(name = "target_currency", nullable = false)
  @Enumerated(EnumType.STRING)
  val targetCurrency: CurrencyInfo,

  @Column(name = "exchange_rate", nullable = false)
  val exchangeRate: Double,

  @Column(name = "fee", nullable = false)
  val fee: BigDecimal,

  @Column(name = "target_amount", nullable = false)
  val targetAmount: BigDecimal,

  @Column(name = "expire_time", nullable = false)
  val expireTime: LocalDateTime,

  @Column(name = "usd_exchange_rate", nullable = false)
  val usdExchangeRate: Double,

  @Column(name = "usd_amount", nullable = false)
  val usdAmount: BigDecimal,

  @Column(name = "requested_date")
  val requestedDate: LocalDateTime? = null,

  @Column(name = "transfer_request", nullable = false)
  val transferRequest: Boolean = false,
) {

  fun updateRequest(): Quotation {
    return Quotation(
      id = this.id,
      user = this.user,
      sourceAmount = this.sourceAmount,
      targetCurrency = this.targetCurrency,
      exchangeRate = this.exchangeRate,
      fee = this.fee,
      targetAmount = this.targetAmount,
      expireTime = this.expireTime,
      usdExchangeRate = this.usdExchangeRate,
      usdAmount = this.usdAmount,
      requestedDate = LocalDateTime.now(ZoneId.of("Asia/Seoul")),
      transferRequest = true
    )
  }

}