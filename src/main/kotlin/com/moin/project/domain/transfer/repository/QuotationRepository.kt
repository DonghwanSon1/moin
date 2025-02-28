package com.moin.project.domain.transfer.repository

import com.moin.project.domain.transfer.Quotation
import org.springframework.data.jpa.repository.JpaRepository

interface QuotationRepository: JpaRepository<Quotation, Long>, QuotationCustomRepository {

  fun findByIdAndUserSnAndTransferRequest(id: Long, userSn: Long, transferRequest: Boolean): Quotation?
}