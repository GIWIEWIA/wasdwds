import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth'; // URL ของ API

  constructor(private http: HttpClient) {}

  // ฟังก์ชันสมัครสมาชิก (Register)
  register(user: { username: string, email: string, password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user, { responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  // ฟังก์ชันเข้าสู่ระบบ (Login)
  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password }).pipe(
        tap(response => {
            if (response.accessToken && response.refreshToken) {
                localStorage.setItem('accessToken', response.accessToken);
                localStorage.setItem('refreshToken', response.refreshToken);
                console.log("🔑 Access Token Stored:", response.accessToken);
            } else {
                console.error("❌ ไม่พบ Access Token หรือ Refresh Token ใน response");
            }

            // ✅ บันทึก email ลง LocalStorage ถ้ามี
            if (response.email) {
                localStorage.setItem('userEmail', response.email);
                console.log("📧 Stored User Email:", response.email);
            } else {
                console.warn("⚠️ No email found in login response");
            }
        }),
        catchError(this.handleError) // ✅ เพิ่มการจัดการ error
    );
}



  // เก็บ accessToken และ refreshToken ใน localStorage
  storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  // ดึง accessToken จาก localStorage
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  // ดึง refreshToken จาก localStorage
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  // ตรวจสอบว่า JWT Token มีหรือไม่ (ใช้ accessToken เป็นตัวตรวจ)
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  // ออกจากระบบ (ลบ accessToken และ refreshToken)
  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  // ฟังก์ชันสำหรับการส่ง Request ที่ต้องการ JWT Token ใน Authorization header
  sendRequestWithToken(url: string, body: any): Observable<any> {
    const token = this.getAccessToken();
    if (!token) {
      return throwError(() => new Error('JWT Token not found'));
    }
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.post(url, body, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  // จัดการ Error จาก API
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'เกิดข้อผิดพลาดในการเชื่อมต่อกับเซิร์ฟเวอร์';

    if (error.status === 0) {
      errorMessage = 'ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้ โปรดตรวจสอบอินเทอร์เน็ตของคุณ';
    } else if (error.status === 400) {
      errorMessage = 'ข้อมูลไม่ถูกต้อง กรุณาตรวจสอบอีกครั้ง';
    } else if (error.status === 401) {
      errorMessage = 'การยืนยันตัวตนล้มเหลว กรุณาตรวจสอบ JWT token';
    } else if (error.status === 500) {
      errorMessage = 'เกิดข้อผิดพลาดภายในระบบ โปรดติดต่อฝ่ายสนับสนุน';
    }

    return throwError(() => new Error(errorMessage));
  }
}
