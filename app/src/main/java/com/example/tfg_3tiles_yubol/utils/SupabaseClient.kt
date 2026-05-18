package com.example.tfg_3tiles_yubol.utils

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.SupabaseClient

// Cliente global de Supabase. La clave es publishable (segura para cliente), no es secreta.
lateinit var supabase: SupabaseClient
    private set

fun initSupabase() {
    supabase = createSupabaseClient(
        supabaseUrl = "https://wpwawqnrsbazewwxxgnr.supabase.co",
        supabaseKey = "sb_publishable_MEP_ePxA2KYM5GIxGneYcw_B3kPC2X8"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
