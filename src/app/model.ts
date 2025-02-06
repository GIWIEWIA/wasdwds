export enum FileStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export interface FileModel {
  id?: number;
  fileName: string;
  filePath: string;
  uploaderId: number;
  fileSize: number;
  uploadDate: string;
  status: FileStatus;
}
