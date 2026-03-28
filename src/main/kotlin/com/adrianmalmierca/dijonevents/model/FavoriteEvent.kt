package com.adrianmalmierca.dijonevents.model

import jakarta.persistence.*

@Entity
@Table(name = "favorite_events")
data class FavoriteEvent(
    @Id
    val openAgendaUid: String, // events uid in OpenAgenda

    @Column(nullable = false)
    val title: String,

    @Column
    val imageUrl: String? = null,

    @Column
    val dateStart: String? = null,

    @Column
    val latitude: Double? = null,

    @Column
    val longitude: Double? = null
)
