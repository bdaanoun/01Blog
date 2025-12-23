import { RouterLink } from '@angular/router';
import { Component } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  imports: [RouterLink],

  styleUrls: ['./header.component.css']
})
export class HeaderComponent { }
