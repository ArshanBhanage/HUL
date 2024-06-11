package com.hul.di

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import com.hul.curriculam.CurriculamComponent
import com.hul.dashboard.DashboardComponent
import com.hul.loginRegistraion.LoginRegisterComponent
import dagger.Module

@Module(subcomponents = [LoginRegisterComponent::class,DashboardComponent::class,CurriculamComponent::class])
class AppSubComponent