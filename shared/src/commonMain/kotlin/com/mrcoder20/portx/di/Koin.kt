package com.mrcoder20.portx.di

import com.mrcoder20.portx.createDatabaseDriver
import com.mrcoder20.portx.data.local.AppDatabase
import com.mrcoder20.portx.data.local.ScanEntity
import com.mrcoder20.portx.data.local.listOfIntAdapter
import com.mrcoder20.portx.data.local.mapIntStringAdapter
import com.mrcoder20.portx.data.local.booleanAdapter
import com.mrcoder20.portx.data.network.PortScanner
import com.mrcoder20.portx.data.network.RemoteApi
import com.mrcoder20.portx.data.network.RemoteApiImpl
import com.mrcoder20.portx.data.repository.ScanRepositoryImpl
import com.mrcoder20.portx.domain.repository.ScanRepository
import com.mrcoder20.portx.domain.usecase.*
import com.mrcoder20.portx.presentation.viewmodel.ReportsViewModel
import com.mrcoder20.portx.presentation.viewmodel.ScanViewModel
import com.mrcoder20.portx.presentation.viewmodel.ToolsViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

fun initKoin() = initKoin {}

val commonModule = module {
    single { 
        val driver = createDatabaseDriver()
        AppDatabase(
            driver = driver,
            ScanEntityAdapter = ScanEntity.Adapter(
                openPortsAdapter = listOfIntAdapter,
                portBannersAdapter = mapIntStringAdapter,
                portServicesAdapter = mapIntStringAdapter
            )
        )
    }
    single {
        com.mrcoder20.portx.domain.SecurityHarden.createSecureClient()
    }
    single<RemoteApi> { RemoteApiImpl(get()) }
    single { PortScanner() }
    single<ScanRepository> { ScanRepositoryImpl(get(), get()) }
    single { com.mrcoder20.portx.domain.SettingsManager(get()) }
    
    factory { ScanPortUseCase(get()) }
    factory { SecurityScoreUseCase() }
    factory { FirewallDetectionUseCase() }
    factory { AnomalyDetectionUseCase() }
    factory { ExportReportUseCase() }
    
    factory { ScanViewModel(get(), get(), get(), get(), get(), get()) }
    factory { ReportsViewModel(get(), get()) }
    factory { ToolsViewModel() }
}
