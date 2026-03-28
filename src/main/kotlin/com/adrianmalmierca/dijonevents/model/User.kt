package com.adrianmalmierca.dijonevents.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_favorites",
        joinColumns = [JoinColumn(name = "user_id")], //user
        inverseJoinColumns = [JoinColumn(name = "event_id")] //favourites
    )
    val favorites: MutableSet<FavoriteEvent> = mutableSetOf() //mutable to add events
)
