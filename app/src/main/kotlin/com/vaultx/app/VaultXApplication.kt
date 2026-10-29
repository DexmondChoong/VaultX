package com.vaultx.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point that initializes the root Dagger-Hilt dependency graph.
 */
@HiltAndroidApp
class VaultXApplication : Application()
