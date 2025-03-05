import { Component, OnInit } from '@angular/core';
import { FileService } from '../../service/file.service';
import { FileData, FileStatus } from '../../model/file_model';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';


@Component({
  selector: 'app-mainpage',
  templateUrl: './mainpage.component.html',
  styleUrls: ['./mainpage.component.css']
})
export class MainpageComponent implements OnInit {
  files: FileData[] = [];
  loggedInUser = { email: '' };

  constructor(private fileService: FileService, private router: Router) {}

  ngOnInit() {
    this.loadUserEmail(); // ✅ ตรวจสอบว่า function นี้มีอยู่
    this.loadUserFiles();
}

// ✅ ตรวจสอบว่ามีฟังก์ชัน loadUserEmail() หรือไม่
loadUserEmail() {
    const userEmail = localStorage.getItem("userEmail") ?? "unknown@example.com";
    this.loggedInUser.email = userEmail;
    console.log("✅ User Email Loaded:", this.loggedInUser.email);
}



  // ✅ โหลดไฟล์ของผู้ใช้
// ตัวอย่างของการโหลดไฟล์จากฐานข้อมูล
loadUserFiles() {
  console.log("🔄 Fetching user files...");

  this.fileService.getUserFiles().subscribe({
    next: (data) => {
      this.files = data.map(file => {
        // ตรวจสอบว่า uploadDate เป็น array หรือไม่
        if (Array.isArray(file.uploadDate)) {
          // แปลงจาก array เป็น Date object
          file.uploadDate = new Date(file.uploadDate[0],
                                     file.uploadDate[1],
                                     file.uploadDate[2],
                                     file.uploadDate[3],
                                     file.uploadDate[4],
                                     file.uploadDate[5],
                                     file.uploadDate[6]);
        }
        // เพิ่ม filePath เข้าไปในไฟล์
        file.filePath = this.getFileUrl(file.filePath); // ใช้ฟังก์ชันเพื่อเพิ่ม URL
        return file;
      });
      console.log("📂 User files loaded (Updated):", this.files);
    },
    error: (error) => {
      console.error("❌ Error fetching user files:", error);
      Swal.fire("เกิดข้อผิดพลาด", "ไม่สามารถโหลดไฟล์ได้", "error");
    }
  });
}

// ฟังก์ชันเพื่อสร้าง URL ของไฟล์
getFileUrl(filePath: string): string {
  // ตรวจสอบให้แน่ใจว่า URL เป็น URL ที่สามารถเข้าถึงจากเบราว์เซอร์
  return `http://localhost:4200/files/${filePath}`;
}










  // ✅ เมื่อเลือกไฟล์จาก Input
  onFilesSelected(event: any): void {
    event.preventDefault();
    const selectedFiles = event.target.files;
    if (!selectedFiles || selectedFiles.length === 0) {
        console.warn("⚠️ No files selected.");
        return;
    }

    // ดึง email จาก Local Storage
    const userEmail = localStorage.getItem("userEmail") ?? "unknown@example.com";
    console.log("📧 Using userEmail for upload:", userEmail);

    for (const file of selectedFiles) {
        const newFile: FileData = {
            fileName: file.name,
            fileType: file.type,
            senderEmail: userEmail,  // ใส่อีเมลที่ถูกต้อง
            uploadDate: new Date().toISOString(),  // เก็บวันที่อัปโหลด
            status: FileStatus.PENDING,  // กำหนดสถานะเป็น PENDING
            localFile: file,
            filePath: '',
            fileUrl: ''
        };
        this.files.push(newFile);  // เพิ่มไฟล์เข้าในรายการ
    }

    console.log("📂 Files selected:", this.files);
    event.target.value = null;  // รีเซ็ต input file
}





  // ✅ ยืนยันและอัปโหลดไฟล์
  confirmFile(file: FileData): void {
    console.log("📅 Confirming file with upload date:", file.uploadDate);

    // ตรวจสอบว่าไฟล์ยังคงมีสถานะเป็น PENDING ก่อนที่จะทำการยืนยัน
    if (file.status === FileStatus.PENDING) {
        file.status = FileStatus.UPLOADING;  // ตั้งค่าเป็น UPLOADING ก่อนส่งไป
        this.fileService.uploadFile(file).subscribe({
            next: (response: FileData) => {
                console.log("📂 File uploaded and confirmed:", response);

                // เมื่อยืนยันแล้ว ให้ใช้สถานะที่ได้รับจาก server
                file.status = response.status;  // อัปเดตสถานะไฟล์จาก server
                Swal.fire("สำเร็จ", "ไฟล์ได้รับการยืนยันแล้ว", "success");

                // รีเฟรชรายการไฟล์
                this.loadUserFiles();
            },
            error: (error) => {
                console.error("❌ Error uploading file", error);
                file.status = FileStatus.FAILED;  // เปลี่ยนสถานะเป็น FAILED
                Swal.fire("เกิดข้อผิดพลาด", "ไม่สามารถอัปโหลดไฟล์ได้", "error");
            }
        });
    }
}




deleteFile(file: FileData): void {
  if (file.status === FileStatus.PENDING) {
      // ถ้าไฟล์ยังเป็น PENDING สามารถลบได้ทันทีจาก UI
      this.files = this.files.filter(f => f.id !== file.id);  // ลบเฉพาะไฟล์ที่เลือกจาก UI
      Swal.fire("ลบสำเร็จ", "ไฟล์ถูกลบจากรายการแล้ว", "success");
  } else {
      // ถ้าไฟล์ได้รับการยืนยันแล้ว, ให้ถามผู้ใช้ก่อนลบ
      Swal.fire({
          title: "คุณแน่ใจหรือไม่?",
          text: "ไฟล์จะถูกลบถาวร!",
          icon: "warning",
          showCancelButton: true,
          confirmButtonText: "ใช่, ลบเลย!",
          cancelButtonText: "ยกเลิก"
      }).then((result) => {
          if (result.isConfirmed) {
              // ส่งคำขอลบไฟล์จาก backend
              this.fileService.deleteFile(file.id!).subscribe({
                  next: () => {
                      // ลบไฟล์จาก UI เฉพาะไฟล์ที่ถูกยืนยันแล้วจาก backend
                      this.files = this.files.filter(f => f.id !== file.id);
                      Swal.fire("ลบสำเร็จ", "ไฟล์ถูกลบเรียบร้อยแล้ว", "success");
                  },
                  error: (error) => {
                      console.error("❌ Error deleting file", error);
                      Swal.fire("เกิดข้อผิดพลาด", "ไม่สามารถลบไฟล์ได้", "error");
                  }
              });
          }
      });
  }
}





viewFile(file: FileData): void {
  if (file.filePath) {
      window.open(file.filePath, "_blank");
  }
}





  // ✅ ออกจากระบบ
  logout(): void {
    Swal.fire({
      title: "คุณต้องการออกจากระบบหรือไม่?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "ตกลง",
      cancelButtonText: "ยกเลิก"
    }).then((result) => {
      if (result.isConfirmed) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userEmail'); // ✅ ล้างข้อมูลอีเมลด้วย
        this.router.navigate(['/login']);
      }
    });
  }
}

