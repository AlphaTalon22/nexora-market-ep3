import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ClusterInfo, ConsumerGroupKafka, TopicoKafka } from '../models/kafka-admin';

@Injectable({ providedIn: 'root' })
export class KafkaAdminService {
  private readonly http = inject(HttpClient);

  cluster(): Observable<ClusterInfo> {
    return this.http.get<ClusterInfo>('/api/kafka-admin/cluster');
  }

  listarTopicos(): Observable<TopicoKafka[]> {
    return this.http.get<TopicoKafka[]>('/api/kafka-admin/topicos');
  }

  consumerGroups(): Observable<ConsumerGroupKafka[]> {
    return this.http.get<ConsumerGroupKafka[]>('/api/kafka-admin/grupos');
  }
}
