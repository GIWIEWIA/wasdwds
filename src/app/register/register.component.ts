import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth_service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading: boolean = false; // ✅ ใช้เพื่อปิด/เปิดปุ่มและ Loading State

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit() {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  register() {
    if (!this.registerForm) {
      this.toastr.error('เกิดข้อผิดพลาดในการโหลดฟอร์ม', 'ข้อผิดพลาด');
      return;
    }

    if (this.registerForm.invalid) {
      this.toastr.warning('กรุณากรอกข้อมูลให้ครบถ้วน', 'ข้อมูลไม่ครบถ้วน');
      return;
    }

    this.isLoading = true; // 🔄 เปิดสถานะ Loading

    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.toastr.success('สมัครสมาชิกสำเร็จ!', 'สำเร็จ');
        this.isLoading = false; // ✅ ปิดสถานะ Loading
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.toastr.error(err.message, 'ข้อผิดพลาด');
        this.isLoading = false; // ✅ ปิดสถานะ Loading
      }
    });
  }
}
