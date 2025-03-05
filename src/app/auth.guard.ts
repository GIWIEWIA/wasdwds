import { inject } from '@angular/core';
import { Router , CanActivateFn} from '@angular/router';


export const authGuard: CanActivateFn = () => {
  const router = inject(Router);

  // ตรวจสอบว่า accessToken มีใน localStorage หรือไม่
  const accessToken = localStorage.getItem('accessToken');

  // ถ้ามี token ก็อนุญาตให้เข้าถึง
  if (accessToken) {
    return true;
  } else {
    // ถ้าไม่มี token, นำทางไปที่หน้า login
    router.navigate(['/login']);
    return false;
  }
};
