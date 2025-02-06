import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginckanComponent } from './loginckan.component';

describe('LoginckanComponent', () => {
  let component: LoginckanComponent;
  let fixture: ComponentFixture<LoginckanComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LoginckanComponent]
    });
    fixture = TestBed.createComponent(LoginckanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
