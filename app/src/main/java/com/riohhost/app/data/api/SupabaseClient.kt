package com.riohhost.app.data.api

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = "https://cwcauobnbmzjpqjmmomc.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN3Y2F1b2JuYm16anBxam1tb21jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTIwMTIzMDEsImV4cCI6MjA2NzU4ODMwMX0.o8hINdVYx2w7kI15hvnKHUzV5FjiIeqfCV4VOpkasow"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}
