package com.example.carspotter.models


enum class SyncState {
    SYNCED,
    PENDING_UPDATE,   // Zmienione offline (np. edycja notatki), czeka na aktualizację
    PENDING_DELETE    // Usunięte offline, czeka na skasowanie
}