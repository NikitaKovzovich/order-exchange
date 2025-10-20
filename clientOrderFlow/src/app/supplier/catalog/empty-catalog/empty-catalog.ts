import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'supplier-empty-catalog',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './empty-catalog.html',
  styleUrls: ['./empty-catalog.css']
})
export class EmptyCatalog {}
