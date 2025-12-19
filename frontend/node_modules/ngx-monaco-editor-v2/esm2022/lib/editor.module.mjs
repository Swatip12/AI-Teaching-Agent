import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NGX_MONACO_EDITOR_CONFIG } from './config';
import { DiffEditorComponent } from './diff-editor.component';
import { EditorComponent } from './editor.component';
import * as i0 from "@angular/core";
export class MonacoEditorModule {
    static forRoot(config = {}) {
        return {
            ngModule: MonacoEditorModule,
            providers: [
                { provide: NGX_MONACO_EDITOR_CONFIG, useValue: config }
            ]
        };
    }
    static { this.ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: MonacoEditorModule, deps: [], target: i0.ɵɵFactoryTarget.NgModule }); }
    static { this.ɵmod = i0.ɵɵngDeclareNgModule({ minVersion: "14.0.0", version: "17.0.3", ngImport: i0, type: MonacoEditorModule, declarations: [EditorComponent,
            DiffEditorComponent], imports: [CommonModule], exports: [EditorComponent,
            DiffEditorComponent] }); }
    static { this.ɵinj = i0.ɵɵngDeclareInjector({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: MonacoEditorModule, imports: [CommonModule] }); }
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: MonacoEditorModule, decorators: [{
            type: NgModule,
            args: [{
                    imports: [
                        CommonModule
                    ],
                    declarations: [
                        EditorComponent,
                        DiffEditorComponent
                    ],
                    exports: [
                        EditorComponent,
                        DiffEditorComponent
                    ]
                }]
        }] });
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZWRpdG9yLm1vZHVsZS5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uL3Byb2plY3RzL2VkaXRvci9zcmMvbGliL2VkaXRvci5tb2R1bGUudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUEsT0FBTyxFQUFFLFlBQVksRUFBRSxNQUFNLGlCQUFpQixDQUFDO0FBQy9DLE9BQU8sRUFBdUIsUUFBUSxFQUFFLE1BQU0sZUFBZSxDQUFDO0FBRTlELE9BQU8sRUFBRSx3QkFBd0IsRUFBeUIsTUFBTSxVQUFVLENBQUM7QUFDM0UsT0FBTyxFQUFFLG1CQUFtQixFQUFFLE1BQU0seUJBQXlCLENBQUM7QUFDOUQsT0FBTyxFQUFFLGVBQWUsRUFBRSxNQUFNLG9CQUFvQixDQUFDOztBQWVyRCxNQUFNLE9BQU8sa0JBQWtCO0lBQ3RCLE1BQU0sQ0FBQyxPQUFPLENBQUMsU0FBZ0MsRUFBRTtRQUN0RCxPQUFPO1lBQ0wsUUFBUSxFQUFFLGtCQUFrQjtZQUM1QixTQUFTLEVBQUU7Z0JBQ1QsRUFBRSxPQUFPLEVBQUUsd0JBQXdCLEVBQUUsUUFBUSxFQUFFLE1BQU0sRUFBRTthQUN4RDtTQUNGLENBQUM7SUFDSixDQUFDOzhHQVJVLGtCQUFrQjsrR0FBbEIsa0JBQWtCLGlCQVIzQixlQUFlO1lBQ2YsbUJBQW1CLGFBSm5CLFlBQVksYUFPWixlQUFlO1lBQ2YsbUJBQW1COytHQUdWLGtCQUFrQixZQVgzQixZQUFZOzsyRkFXSCxrQkFBa0I7a0JBYjlCLFFBQVE7bUJBQUM7b0JBQ1IsT0FBTyxFQUFFO3dCQUNQLFlBQVk7cUJBQ2I7b0JBQ0QsWUFBWSxFQUFFO3dCQUNaLGVBQWU7d0JBQ2YsbUJBQW1CO3FCQUNwQjtvQkFDRCxPQUFPLEVBQUU7d0JBQ1AsZUFBZTt3QkFDZixtQkFBbUI7cUJBQ3BCO2lCQUNGIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IHsgQ29tbW9uTW9kdWxlIH0gZnJvbSAnQGFuZ3VsYXIvY29tbW9uJztcbmltcG9ydCB7IE1vZHVsZVdpdGhQcm92aWRlcnMsIE5nTW9kdWxlIH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5cbmltcG9ydCB7IE5HWF9NT05BQ09fRURJVE9SX0NPTkZJRywgTmd4TW9uYWNvRWRpdG9yQ29uZmlnIH0gZnJvbSAnLi9jb25maWcnO1xuaW1wb3J0IHsgRGlmZkVkaXRvckNvbXBvbmVudCB9IGZyb20gJy4vZGlmZi1lZGl0b3IuY29tcG9uZW50JztcbmltcG9ydCB7IEVkaXRvckNvbXBvbmVudCB9IGZyb20gJy4vZWRpdG9yLmNvbXBvbmVudCc7XG5cbkBOZ01vZHVsZSh7XG4gIGltcG9ydHM6IFtcbiAgICBDb21tb25Nb2R1bGVcbiAgXSxcbiAgZGVjbGFyYXRpb25zOiBbXG4gICAgRWRpdG9yQ29tcG9uZW50LFxuICAgIERpZmZFZGl0b3JDb21wb25lbnRcbiAgXSxcbiAgZXhwb3J0czogW1xuICAgIEVkaXRvckNvbXBvbmVudCxcbiAgICBEaWZmRWRpdG9yQ29tcG9uZW50XG4gIF1cbn0pXG5leHBvcnQgY2xhc3MgTW9uYWNvRWRpdG9yTW9kdWxlIHtcbiAgcHVibGljIHN0YXRpYyBmb3JSb290KGNvbmZpZzogTmd4TW9uYWNvRWRpdG9yQ29uZmlnID0ge30pOiBNb2R1bGVXaXRoUHJvdmlkZXJzPE1vbmFjb0VkaXRvck1vZHVsZT4ge1xuICAgIHJldHVybiB7XG4gICAgICBuZ01vZHVsZTogTW9uYWNvRWRpdG9yTW9kdWxlLFxuICAgICAgcHJvdmlkZXJzOiBbXG4gICAgICAgIHsgcHJvdmlkZTogTkdYX01PTkFDT19FRElUT1JfQ09ORklHLCB1c2VWYWx1ZTogY29uZmlnIH1cbiAgICAgIF1cbiAgICB9O1xuICB9XG59XG4iXX0=