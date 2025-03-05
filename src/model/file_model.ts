export enum FileStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  UPLOADING = 'UPLOADING',
  FAILED = 'FAILED'
}

export interface FileData {
filePath: any;
  id?: number;
  fileName: string;
  fileUrl: string; // ✅ ใช้ fileUrl แทน filePath
  fileType: string; // ✅ เพิ่ม fileType
  senderEmail: string;
  uploadDate: string | Date;
  status: FileStatus;
  localFile?: File; // ✅ ใช้เฉพาะตอนอัปโหลดไฟล์
}
