import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http'; // สำหรับ API
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { LoginckanComponent } from './loginckan/loginckan.component';
import { RegisterComponent } from './register/register.component';
import { ForgetpasswordComponent } from './forgetpassword/forgetpassword.component';
import { CommonModule } from '@angular/common';
import {  ToastrModule } from 'ngx-toastr';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MainpageComponent } from './mainpage/mainpage.component';
import { SitebarComponent } from './sitebar/sitebar.component';


@NgModule({
  declarations: [
    AppComponent,
    LoginckanComponent,
    RegisterComponent,
    ForgetpasswordComponent,
    MainpageComponent,
    SitebarComponent,



  ],
  imports: [
    ReactiveFormsModule,
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    CommonModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      timeOut: 3000,               // การแสดง 3 วินาที
      positionClass: 'toast-top-right', // แสดงที่มุมขวาบน
      preventDuplicates: true,    // ป้องกันการแสดงข้อความซ้ำ
    })


  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
