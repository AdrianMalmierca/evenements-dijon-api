package com.adrianmalmierca.dijonevents.repository

import com.adrianmalmierca.dijonevents.model.FavoriteEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FavoriteEventRepository : JpaRepository<FavoriteEvent, String>
