import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth_service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

@Component({
  selector: 'app-loginckan',
  templateUrl: './loginckan.component.html',
  styleUrls: ['./loginckan.component.css']
})
export class LoginckanComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading: boolean = false;  // สถานะการโหลด

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,  // ใช้ AuthService
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit() {
    // กำหนดฟอร์มสำหรับล็อกอิน
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],  // ฟิลด์อีเมล
      password: ['', Validators.required]  // ฟิลด์รหัสผ่าน
    });
  }

  // ฟังก์ชันสำหรับการเข้าสู่ระบบ
  login() {
    if (this.loginForm.invalid) {
      this.toastr.warning('กรุณากรอกอีเมลและรหัสผ่านให้ครบถ้วน', 'ข้อมูลไม่ครบถ้วน');
      return;
    }

    this.isLoading = true;

    const email = this.loginForm.value.email;
    const password = this.loginForm.value.password;

    console.log("🔍 Sending Login Request:", { email, password });

    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log("✅ API Response:", response);

        if (response.accessToken && response.refreshToken) {
          this.authService.storeTokens(response.accessToken, response.refreshToken);

          console.log("📌 Access Token Stored:", localStorage.getItem('accessToken'));
          console.log("📌 Refresh Token Stored:", localStorage.getItem('refreshToken'));

          // ✅ ดึง Email จาก Token แล้วเก็บใน Local Storage
          try {
            const tokenPayload = JSON.parse(atob(response.accessToken.split('.')[1]));
            if (tokenPayload.email) {
              localStorage.setItem('userEmail', tokenPayload.email);
              console.log("📧 Stored User Email:", tokenPayload.email);
            } else {
              console.warn("⚠️ No email found in token payload");
            }
          } catch (error) {
            console.error("❌ Error decoding token:", error);
          }

          this.isLoading = false;
          this.router.navigate(['/mainpage']);  // ไปที่หน้า MainPage หลังจากล็อกอินสำเร็จ
        } else {
          this.toastr.error('ไม่มี token ใน response', 'ข้อผิดพลาด');
          console.error("🚨 No token in API response");
          this.isLoading = false;
        }
      },
      error: (err) => {
        console.error('❌ Error occurred:', err);
        this.toastr.error(err.message || 'เกิดข้อผิดพลาดในการเข้าสู่ระบบ', 'ข้อผิดพลาด');
        this.isLoading = false;
      }
    });
  }
}
