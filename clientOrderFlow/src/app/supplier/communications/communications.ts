import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Message {
  sender: string;
  text: string;
  time: string;
}

interface Chat {
  id: number;
  name: string;
  lastMessage: string;
  lastMessageTime: string;
  unread: number;
  status: string;
  messages: Message[];
}

@Component({
  selector: 'app-communications',
  imports: [CommonModule, FormsModule],
  templateUrl: './communications.html',
  styleUrl: './communications.css'
})
export class Communications implements OnInit {
  searchQuery: string = '';
  selectedChat: Chat | null = null;
  newMessage: string = '';

  chats: Chat[] = [
    {
      id: 1,
      name: 'Сеть Магазинов',
      lastMessage: 'Когда ожидается поставка?',
      lastMessageTime: '10:23',
      unread: 2,
      status: 'Онлайн',
      messages: [
        { sender: 'them', text: 'Добрый день! Интересует молочная продукция', time: '10:15' },
        { sender: 'me', text: 'Здравствуйте! Да, у нас есть в наличии', time: '10:18' },
        { sender: 'them', text: 'Когда ожидается поставка?', time: '10:23' }
      ]
    },
    {
      id: 2,
      name: 'Супермаркет "Угол"',
      lastMessage: 'Спасибо за быструю доставку!',
      lastMessageTime: 'Вчера',
      unread: 0,
      status: 'Был(а) 2 часа назад',
      messages: [
        { sender: 'them', text: 'Получили заказ', time: 'Вчера 14:30' },
        { sender: 'them', text: 'Спасибо за быструю доставку!', time: 'Вчера 14:31' },
        { sender: 'me', text: 'Рады сотрудничеству!', time: 'Вчера 15:00' }
      ]
    },
    {
      id: 3,
      name: 'Гипермаркет "Центр"',
      lastMessage: 'Нужно обсудить новый заказ',
      lastMessageTime: '28.09',
      unread: 0,
      status: 'Был(а) вчера',
      messages: [
        { sender: 'them', text: 'Нужно обсудить новый заказ', time: '28.09 16:45' },
        { sender: 'me', text: 'Конечно, когда вам удобно?', time: '28.09 17:00' }
      ]
    }
  ];

  ngOnInit() {
  }

  selectChat(chat: Chat) {
    this.selectedChat = chat;
    chat.unread = 0;
  }

  sendMessage() {
    if (this.newMessage.trim() && this.selectedChat) {
      const now = new Date();
      const time = `${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')}`;

      this.selectedChat.messages.push({
        sender: 'me',
        text: this.newMessage,
        time: time
      });

      this.selectedChat.lastMessage = this.newMessage;
      this.selectedChat.lastMessageTime = time;

      this.newMessage = '';
    }
  }
}
