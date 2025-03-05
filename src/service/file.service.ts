import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { FileData, FileStatus } from '../model/file_model';
import { catchError, tap } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class FileService {
  private apiUrl = 'http://localhost:8080/api/files';

  constructor(private http: HttpClient) {}

  // ✅ ฟังก์ชันกลางสำหรับดึง Authorization Header
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.warn("⚠️ Warning: No access token found!");
      return new HttpHeaders(); // ส่ง Header เปล่า ป้องกัน Error
    }
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }




  // ✅ ดึงไฟล์ของผู้ใช้
  getUserFiles(): Observable<FileData[]> {
    const token = localStorage.getItem('accessToken');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    console.log("🔄 Fetching user files...");

    return this.http.get<FileData[]>(`${this.apiUrl}/user`, { headers }).pipe(
        tap(response => {
            console.log("✅ Files received from API:", response);
        }),
        catchError(error => {
            console.error("❌ API Error:", error);
            return throwError(() => new Error(error));
        })
    );
}




uploadFile(file: FileData): Observable<FileData> {
  if (!file.localFile) {
      console.error("❌ Error: localFile is undefined!");
      return throwError(() => new Error("File is missing"));
  }

  const formData = new FormData();
  formData.append('file', file.localFile as Blob);
  formData.append('senderEmail', file.senderEmail || 'unknown@example.com');
  formData.append('fileType', file.fileType || '');

  const token = localStorage.getItem('accessToken');
  const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

  return this.http.post<FileData>(`${this.apiUrl}/upload`, formData, { headers }).pipe(
      tap(response => console.log("✅ File uploaded and confirmed:", response.status)) // ✅ ตรวจสอบว่ารับ `CONFIRMED`
  );
}




  // ✅ อัปเดตสถานะไฟล์
  updateFileStatus(fileId: number, status: FileStatus): Observable<FileData> {
    return this.http.put<FileData>(`${this.apiUrl}/update-status/${fileId}`, { status }, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  // ✅ ลบไฟล์จาก Backend

  deleteFile(fileId: number): Observable<void> {
    console.log("🗑️ กำลังส่งคำขอ DELETE ไปที่ API:", fileId);
    const token = localStorage.getItem('accessToken');

    if (!token) {
        console.error("❌ ไม่มี Token ใน LocalStorage");
        return throwError(() => new Error("Unauthorized: No Token Available"));
    }

    const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    });

    return this.http.delete<void>(`${this.apiUrl}/${fileId}`, { headers });
}



  // ✅ ฟังก์ชันจัดการ Error
  private handleError(error: any): Observable<never> {
    console.error("❌ API Error:", error);
    return throwError(() => new Error(error.message || "An error occurred"));
  }
}
