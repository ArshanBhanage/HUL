package com.hul.di

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import com.hul.curriculam.CurriculamComponent
import com.hul.dashboard.DashboardComponent
import com.hul.loginRegistraion.LoginRegisterComponent
import com.hul.salg.SalgDashboardComponent
import com.hul.sb.SBDashboardComponent
import com.hul.sb.SBSupervisorDashboardComponent
import com.hul.sb.mobiliser.SBMobiliserDashboard
import com.hul.web_form.WebFormComponent
import dagger.Module

@Module(subcomponents = [LoginRegisterComponent::class,DashboardComponent::class, SalgDashboardComponent::class,CurriculamComponent::class, WebFormComponent::class, SBDashboardComponent::class, SBSupervisorDashboardComponent::class])
class AppSubComponent