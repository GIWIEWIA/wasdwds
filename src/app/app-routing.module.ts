import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginckanComponent } from './loginckan/loginckan.component';
import { RegisterComponent } from './register/register.component';
import { ForgetpasswordComponent } from './forgetpassword/forgetpassword.component';
import { MainpageComponent } from './mainpage/mainpage.component';


const routes: Routes = [
{ path: 'login', component: LoginckanComponent},
{ path: 'register', component: RegisterComponent},
{ path: 'forgetpassword', component: ForgetpasswordComponent},
{ path: 'mainpage', component:MainpageComponent},




];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
