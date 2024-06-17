package com.hul.di

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import android.content.Context
import com.hul.camera.CameraComponent
import com.hul.curriculam.CurriculamComponent
import com.hul.dashboard.DashboardComponent
import com.hul.loginRegistraion.LoginRegisterComponent
import com.hul.screens.field_auditor_dashboard.FieldAuditorDashboardComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [AppSubComponent::class, NetworkModule::class, StorageModule::class, DatabaseModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): AppComponent
    }

    fun loginRegisterComponent(): LoginRegisterComponent.Factory

    fun dashboardComponent(): DashboardComponent.Factory

    fun cameraComponent(): CameraComponent.Factory

    fun curriculamComponent(): CurriculamComponent.Factory

    fun fieldAuditorDashboardComponent(): FieldAuditorDashboardComponent.Factory

}