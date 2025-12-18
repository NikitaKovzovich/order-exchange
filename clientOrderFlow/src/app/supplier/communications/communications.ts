import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../services/auth.service';
import { ChatChannel, ChatMessage } from '../../models/api.models';

@Component({
  selector: 'app-communications',
  imports: [CommonModule, FormsModule],
  templateUrl: './communications.html',
  styleUrl: './communications.css'
})
export class Communications implements OnInit {
  searchQuery: string = '';
  selectedChannel: ChatChannel | null = null;
  messages: ChatMessage[] = [];
  newMessage: string = '';
  isLoading: boolean = false;
  channels: ChatChannel[] = [];
  currentUserId: number = 0;

  constructor(
    private chatService: ChatService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    if (user?.userId) {
      this.currentUserId = user.userId;
    }
    this.loadChannels();
  }

  loadChannels() {
    this.isLoading = true;
    this.chatService.getMyChannels().subscribe({
      next: (channels) => {
        this.channels = channels;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading channels:', error);
        this.isLoading = false;
      }
    });
  }

  selectChat(channel: ChatChannel) {
    this.selectedChannel = channel;
    this.loadMessages(channel.orderId);
  }

  loadMessages(orderId: number) {
    this.chatService.getMessages(orderId).subscribe({
      next: (response) => {
        this.messages = response.content;
        this.markAsRead(orderId);
      },
      error: (error) => console.error('Error loading messages:', error)
    });
  }

  markAsRead(orderId: number) {
    this.chatService.markAsRead(orderId).subscribe();
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.selectedChannel) return;

    this.chatService.sendMessage(this.selectedChannel.orderId, {
      messageText: this.newMessage
    }).subscribe({
      next: (message) => {
        this.messages.push(message);
        this.newMessage = '';
        this.loadChannels();
      },
      error: (error) => console.error('Error sending message:', error)
    });
  }

  get filteredChannels(): ChatChannel[] {
    if (!this.searchQuery) return this.channels;
    return this.channels.filter(c =>
      c.channelName.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  formatTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    if (isToday) {
      return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' });
  }
}
