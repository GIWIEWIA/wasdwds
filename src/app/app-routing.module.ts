import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginckanComponent } from './loginckan/loginckan.component';  // หน้า Login
import { RegisterComponent } from './register/register.component';  // หน้า Register
import { ForgetpasswordComponent } from './forgetpassword/forgetpassword.component';  // หน้า Forgetpassword
import { MainpageComponent } from './mainpage/mainpage.component';
import { authGuard } from './auth.guard';  // ใช้ authGuard ในการป้องกัน


const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },  // เริ่มต้นที่หน้า Login
  { path: 'login', component: LoginckanComponent },  // หน้า Login
  { path: 'register', component: RegisterComponent },  // หน้า Register
  { path: 'forgetpassword', component: ForgetpasswordComponent },  // หน้า Forget Password
  { path: 'mainpage' , component: MainpageComponent,canActivate: [authGuard]},

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
