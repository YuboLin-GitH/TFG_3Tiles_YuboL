package com.example.tfg_3tiles_yubol.utils

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest


val supabase = createSupabaseClient(
    supabaseUrl = "https://wpwawqnrsbazewwxxgnr.supabase.co",
    supabaseKey = "sb_publishable_MEP_ePxA2KYM5GIxGneYcw_B3kPC2X8"
) {
    install(Auth)
    install(Postgrest)
}