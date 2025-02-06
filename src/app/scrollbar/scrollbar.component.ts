import { Component } from '@angular/core';

@Component({
  selector: 'app-scrollbar',
  templateUrl: './scrollbar.component.html',
  styleUrls: ['./scrollbar.component.css']
})
export class ScrollbarComponent {
  items = Array.from({ length: 30 }, (_, i) => `This is item #${i + 1}`);

  
}
