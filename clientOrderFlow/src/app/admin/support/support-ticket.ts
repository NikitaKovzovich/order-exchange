import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';

interface Message {
  sender: 'user' | 'admin';
  senderName: string;
  text: string;
  time: string;
}

@Component({
  selector: 'admin-support-ticket',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './support-ticket.html',
  styleUrls: ['./support-ticket.css']
})
export class SupportTicket implements OnInit {
  ticketId: string = '';
  ticketStatus: string = 'new';
  newMessage: string = '';

  messages: Message[] = [
    {
      sender: 'user',
      senderName: 'Сеть Магазинов',
      text: 'Добрый день! Не получается загрузить подписанный УПД по заказу #10230. При попытке загрузки возникает ошибка "Неверный формат файла", хотя я загружаю PDF, как обычно. Помогите, пожалуйста.',
      time: '16.10.2025, 10:15'
    },
    {
      sender: 'admin',
      senderName: 'Администратор',
      text: 'Добрый день! Проверили логи, видим, что файл имеет неверное расширение ".pdf.zip". Пожалуйста, убедитесь, что вы загружаете именно PDF-файл, а не архив. Если проблема повторится, дайте знать.',
      time: '16.10.2025, 10:25'
    }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.ticketId = this.route.snapshot.paramMap.get('id') || '';
  }

  sendMessage() {
    if (this.newMessage.trim()) {
      const now = new Date();
      this.messages.push({
        sender: 'admin',
        senderName: 'Администратор',
        text: this.newMessage,
        time: `${now.toLocaleDateString('ru-RU')}, ${now.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`
      });
      this.newMessage = '';
    }
  }
}
