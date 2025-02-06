
import { Component } from '@angular/core';
import { FileService } from '../file.service';
@Component({
  selector: 'app-mainpage',
  templateUrl: './mainpage.component.html',
  styleUrls: ['./mainpage.component.css']
})
export class MainpageComponent {
  constructor(private fileService: FileService) {}
  filedata? : FileList




  selectedFiles: { name: string; size: number; isUploading: boolean; progress: number; uploadSpeed: number; startTime: number }[] = []; // โครงสร้างไฟล์
  submittedFiles: { name: string; size: number }[] = []; // เก็บไฟล์ที่ถูกส่งไปยัง Listfile
  errorMessage: string = '';
  fileCategories: {
    images: { name: string; size: number }[];
    documents: { name: string; size: number }[];
    others: { name: string; size: number }[];
  } = { images: [], documents: [], others: [] };

  categorizeFiles(): void {
    // รีเซ็ตหมวดหมู่ไฟล์
    this.fileCategories = {
      images: [],
      documents: [],
      others: []
    };

    // จำแนกไฟล์ใน submittedFiles
    this.submittedFiles.forEach((file) => {
      const extension = file.name.split('.').pop()?.toLowerCase(); // ดึงนามสกุลไฟล์หลังจุด

      if (extension) {
        if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(extension)) {
          this.fileCategories.images.push(file);
        } else if (['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt'].includes(extension)) {
          this.fileCategories.documents.push(file);
        } else {
          this.fileCategories.others.push(file);
        }
      } else {
        // หากไม่มีนามสกุลไฟล์ ให้จัดไปที่ "others"
        this.fileCategories.others.push(file);
      }
    });
  }

  getFileIcon(fileName: string): string {
    const extension = fileName.split('.').pop()?.toLowerCase(); // ดึงนามสกุลไฟล์หลังจุด

    if (extension) {
      if (['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(extension)) {
        return '../../assets/img/Picture.png'; // ไอคอนสำหรับไฟล์รูปภาพ
      } else if (['pdf'].includes(extension)) {
        return '../../assets/img/pdf.png'; // ไอคอนสำหรับ PDF
      } else if (['doc', 'docx'].includes(extension)) {
        return '../../assets/img/word.png'; // ไอคอนสำหรับ Word
      } else if (['ppt', 'pptx'].includes(extension)) {
        return '../../assets/img/PowerPoint'; // ไอคอนสำหรับ PowerPoint
      } else if (['xls', 'xlsx'].includes(extension)) {
        return '../../assets/img/Excel'; // ไอคอนสำหรับ Excel
      } else {
        return '../../assets/img/UnknowFile.png'; // ไอคอนสำหรับไฟล์ที่ไม่รู้จัก
      }

    }
    return '../../assets/img/File_no_Extentions.png'; // กรณีไม่มีนามสกุลไฟล์

  }

  handleFileInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.filedata = input.files;
      this.selectedFiles = Array.from(input.files);
      console.log("📌 ไฟล์ที่เลือก:", this.selectedFiles);
    } else {
      console.log("❌ ไม่มีไฟล์ที่ถูกเลือก");
    }
  }



  // เปิดหน้าต่างเลือกไฟล์
  triggerFileUpload(): void {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.multiple = true;
    fileInput.accept = '*/*';
    fileInput.onchange = (event: Event) => this.handleFileInput(event);
    fileInput.click();
  }


  // จัดการไฟล์ที่เลือก

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
        const files: FileList = input.files;

        for (let i = 0; i < files.length; i++) {
            const file = files[i];

            // ตรวจสอบไฟล์ซ้ำใน selectedFiles หรือ submittedFiles
            const isDuplicate = this.selectedFiles.some(existingFile => existingFile.name === file.name) ||
                                this.submittedFiles.some(existingFile => existingFile.name === file.name);

            console.log(`File ${i + 1}:`);
            console.log('Name:', file.name);
            console.log('Size:', file.size, 'bytes');
            console.log('Type:', file.type);
            console.log('Last Modified:', new Date(file.lastModified));
            console.log('-----------------------');
            console.log('Files selected:', input.files);
            console.log('Current selected files:', this.selectedFiles);

            if (isDuplicate) {
                // ถ้ามีไฟล์ซ้ำ ให้แจ้งเตือน
                this.errorMessage = `File "${file.name}" is already added.`;
                setTimeout(() => (this.errorMessage = ''), 3000); // ลบข้อความหลัง 3 วินาที
                continue; // ข้ามการเพิ่มไฟล์นี้
            }

            const newFile = {
                name: file.name,
                size: file.size,
                isUploading: true,
                progress: 0,
                uploadSpeed: 1024 * 1024, // 1 MB/s
                startTime: Date.now()
            };

            this.selectedFiles.push(newFile);

            // ฟังก์ชันจำลองการอัปโหลด
            this.simulateUpload(newFile);
        }
    } else {
        console.log('No files selected.');
        this.selectedFiles = [];
    }
}


  // ฟังก์ชันจำลองการอัปโหลด
  simulateUpload(file: { name: string; size: number; isUploading: boolean; progress: number; uploadSpeed: number; startTime: number }): void {
    const updateProgress = () => {
      if (file.progress < 100) {
        const elapsedTime = (Date.now() - file.startTime) / 1000; // เวลาในวินาที
        const bytesUploaded = Math.min(file.size, file.uploadSpeed * elapsedTime); // คำนวณขนาดไฟล์ที่อัปโหลดได้
        file.progress = (bytesUploaded / file.size) * 100; // คำนวณเปอร์เซ็นต์การอัปโหลด

        // แสดงเวลาที่ใช้
        const timeLeft = Math.max((file.size - bytesUploaded) / file.uploadSpeed, 0);
        console.log(`Uploading ${file.name}: ${Math.round(file.progress)}% complete. Time remaining: ${Math.round(timeLeft)} seconds.`);

        requestAnimationFrame(updateProgress); // เรียกใช้ฟังก์ชันในรอบถัดไป
      } else {
        file.isUploading = false;
        console.log(`Upload of ${file.name} complete.`);
      }
    };

    updateProgress(); // เริ่มต้นการอัปเดต
  }

  // ลบไฟล์ที่เลือก
  removeFile(event: MouseEvent, index: number): void {
    event.stopPropagation(); // หยุดการแพร่กระจายของเหตุการณ์คลิก
    this.selectedFiles.splice(index, 1);
    console.log(`File at index ${index} removed.`);
  }


  // จัดการลากไฟล์เข้า (Drag Over)
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'copy'; // แสดงไอคอนคัดลอก
  }

  // จัดการไฟล์ที่ลากมาวาง (Drop)
  onDrop(event: DragEvent): void {
    event.preventDefault();


    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      const files: FileList = event.dataTransfer.files;

      this.filedata = files
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        console.log(`Dropped File ${i + 1}:`);
        console.log('Name:', file.name);
        console.log('Size:', file.size, 'bytes');
        console.log('Type:', file.type);
        console.log('Last Modified:', new Date(file.lastModified));
        console.log('-----------------------');

        const newFile = {
          name: file.name,
          size: file.size,
          isUploading: true,
          progress: 0,
          uploadSpeed: 1024 * 1024, // 1 MB/s
          startTime: Date.now()
        };

        this.selectedFiles.push(newFile);

        // ฟังก์ชันจำลองการอัปโหลด
        this.simulateUpload(newFile);
      }

      event.dataTransfer.clearData(); // ล้างข้อมูลหลังจากประมวลผลเสร็จ
    }
  }

  submitFiles(): void {
    console.log("📌 ตรวจสอบไฟล์ก่อนส่ง:", this.selectedFiles);

    if (this.selectedFiles.length === 0) {
        this.errorMessage = "❌ ไม่มีไฟล์ที่ถูกเลือก";
        return;
    }

    const formData = new FormData();
    this.selectedFiles.forEach((file) => {
        console.log(`📤 กำลังเพิ่มไฟล์ลงใน FormData: ${file.name} (${file.size} bytes)`);
        formData.append('files', file, file.name);
    });

    console.log("📤 ตรวจสอบ FormData ก่อนส่ง:", formData);

    this.fileService.uploadFile(formData).subscribe({
        next: (response) => {
            console.log("✅ อัปโหลดสำเร็จ:", response);
            this.selectedFiles = [];
            this.filedata = undefined;
            this.errorMessage = '';
        },
        error: (error) => console.error("❌ อัปโหลดล้มเหลว:", error),
    });
}




  ngOnInit(): void {
    const storedFiles = localStorage.getItem('submittedFiles');
    if (storedFiles) {
        this.submittedFiles = JSON.parse(storedFiles);
        this.categorizeFiles(); // จัดหมวดหมู่ไฟล์ที่โหลดมา
    }
}






}
