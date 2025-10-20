import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-retail-communications',
  imports: [CommonModule, FormsModule],
  templateUrl: './communications.html',
  styleUrl: './communications.css'
})
export class Communications {
  selectedChat: number | null = null;
  newMessage: string = '';

  chats = [
    {
      id: 1,
      supplier: 'Продукты Оптом',
      lastMessage: 'Заказ готов к отправке',
      time: '10:30',
      unread: 2,
      online: true
    },
    {
      id: 2,
      supplier: 'Молочная Ферма',
      lastMessage: 'Спасибо за заказ!',
      time: 'Вчера',
      unread: 0,
      online: false
    },
    {
      id: 3,
      supplier: 'Хлебозавод №1',
      lastMessage: 'Когда планируете следующий заказ?',
      time: '15.10.2025',
      unread: 1,
      online: true
    }
  ];

  messages: { [key: number]: any[] } = {
    1: [
      { id: 1, from: 'supplier', text: 'Здравствуйте! Ваш заказ №12345 принят в обработку', time: '09:00', date: '20.10.2025' },
      { id: 2, from: 'me', text: 'Спасибо! Когда ожидается отправка?', time: '09:15', date: '20.10.2025' },
      { id: 3, from: 'supplier', text: 'Планируем отправить сегодня до 16:00', time: '09:20', date: '20.10.2025' },
      { id: 4, from: 'supplier', text: 'Заказ готов к отправке', time: '10:30', date: '20.10.2025' }
    ],
    2: [
      { id: 1, from: 'me', text: 'Добрый день! Хочу оформить заказ', time: '14:00', date: '19.10.2025' },
      { id: 2, from: 'supplier', text: 'Здравствуйте! С удовольствием поможем', time: '14:05', date: '19.10.2025' },
      { id: 3, from: 'me', text: 'Отлично, отправил заказ через систему', time: '14:10', date: '19.10.2025' },
      { id: 4, from: 'supplier', text: 'Спасибо за заказ!', time: '14:15', date: '19.10.2025' }
    ],
    3: [
      { id: 1, from: 'supplier', text: 'Здравствуйте! Когда планируете следующий заказ?', time: '11:00', date: '15.10.2025' }
    ]
  };

  selectChat(chatId: number) {
    this.selectedChat = chatId;
    const chat = this.chats.find(c => c.id === chatId);
    if (chat) {
      chat.unread = 0;
    }
  }

  sendMessage() {
    if (!this.newMessage.trim() || this.selectedChat === null) return;

    const newMsg = {
      id: this.messages[this.selectedChat].length + 1,
      from: 'me',
      text: this.newMessage,
      time: new Date().toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' }),
      date: new Date().toLocaleDateString('ru-RU')
    };

    this.messages[this.selectedChat].push(newMsg);

    const chat = this.chats.find(c => c.id === this.selectedChat);
    if (chat) {
      chat.lastMessage = this.newMessage;
      chat.time = newMsg.time;
    }

    this.newMessage = '';
  }

  get selectedChatData() {
    return this.chats.find(c => c.id === this.selectedChat);
  }

  get selectedMessages() {
    return this.selectedChat !== null ? this.messages[this.selectedChat] : [];
  }
}
