import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

@Injectable({ providedIn: "root" })
export class AdminService {
    private baseUrl = "http://localhost:8080/api/admin";

    constructor(private http: HttpClient) { }

    deletePostReport(reportId: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/reports/${reportId}`);
    }

    deleteUserReport(reportId: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/reports/${reportId}`);
    }
}
