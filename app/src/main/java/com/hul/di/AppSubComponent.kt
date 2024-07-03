package com.hul.di

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import com.hul.curriculam.CurriculamComponent
import com.hul.dashboard.DashboardComponent
import com.hul.loginRegistraion.LoginRegisterComponent
import com.hul.web_form.WebFormComponent
import dagger.Module

@Module(subcomponents = [LoginRegisterComponent::class,DashboardComponent::class,CurriculamComponent::class, WebFormComponent::class])
class AppSubComponent