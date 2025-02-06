import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http'; // สำหรับ API
import { FormsModule } from '@angular/forms';
import { LoginckanComponent } from './loginckan/loginckan.component';
import { RegisterComponent } from './register/register.component';
import { ForgetpasswordComponent } from './forgetpassword/forgetpassword.component';
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
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
