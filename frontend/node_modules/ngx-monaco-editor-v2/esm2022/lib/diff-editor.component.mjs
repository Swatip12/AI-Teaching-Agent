import { Component, Inject, Input } from '@angular/core';
import { fromEvent } from 'rxjs';
import { BaseEditor } from './base-editor';
import { NGX_MONACO_EDITOR_CONFIG } from './config';
import * as i0 from "@angular/core";
export class DiffEditorComponent extends BaseEditor {
    set options(options) {
        this._options = Object.assign({}, this.config.defaultOptions, options);
        if (this._editor) {
            this._editor.dispose();
            this.initMonaco(options, this.insideNg);
        }
    }
    get options() {
        return this._options;
    }
    set originalModel(model) {
        this._originalModel = model;
        if (this._editor) {
            this._editor.dispose();
            this.initMonaco(this.options, this.insideNg);
        }
    }
    set modifiedModel(model) {
        this._modifiedModel = model;
        if (this._editor) {
            this._editor.dispose();
            this.initMonaco(this.options, this.insideNg);
        }
    }
    constructor(zone, editorConfig) {
        super(editorConfig);
        this.zone = zone;
        this.editorConfig = editorConfig;
    }
    initMonaco(options, insideNg) {
        if (!this._originalModel || !this._modifiedModel) {
            throw new Error('originalModel or modifiedModel not found for ngx-monaco-diff-editor');
        }
        this._originalModel.language = this._originalModel.language || options.language;
        this._modifiedModel.language = this._modifiedModel.language || options.language;
        let originalModel = monaco.editor.createModel(this._originalModel.code, this._originalModel.language);
        let modifiedModel = monaco.editor.createModel(this._modifiedModel.code, this._modifiedModel.language);
        this._editorContainer.nativeElement.innerHTML = '';
        const theme = options.theme;
        if (insideNg) {
            this._editor = monaco.editor.createDiffEditor(this._editorContainer.nativeElement, options);
        }
        else {
            this.zone.runOutsideAngular(() => {
                this._editor = monaco.editor.createDiffEditor(this._editorContainer.nativeElement, options);
            });
        }
        options.theme = theme;
        this._editor.setModel({
            original: originalModel,
            modified: modifiedModel
        });
        // refresh layout on resize event.
        if (this._windowResizeSubscription) {
            this._windowResizeSubscription.unsubscribe();
        }
        this._windowResizeSubscription = fromEvent(window, 'resize').subscribe(() => this._editor.layout());
        this.onInit.emit(this._editor);
    }
    static { this.ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: DiffEditorComponent, deps: [{ token: i0.NgZone }, { token: NGX_MONACO_EDITOR_CONFIG }], target: i0.ɵɵFactoryTarget.Component }); }
    static { this.ɵcmp = i0.ɵɵngDeclareComponent({ minVersion: "14.0.0", version: "17.0.3", type: DiffEditorComponent, selector: "ngx-monaco-diff-editor", inputs: { options: "options", originalModel: "originalModel", modifiedModel: "modifiedModel" }, usesInheritance: true, ngImport: i0, template: '<div class="editor-container" #editorContainer></div>', isInline: true, styles: [":host{display:block;height:200px}.editor-container{width:100%;height:98%}\n"] }); }
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: DiffEditorComponent, decorators: [{
            type: Component,
            args: [{ selector: 'ngx-monaco-diff-editor', template: '<div class="editor-container" #editorContainer></div>', styles: [":host{display:block;height:200px}.editor-container{width:100%;height:98%}\n"] }]
        }], ctorParameters: () => [{ type: i0.NgZone }, { type: undefined, decorators: [{
                    type: Inject,
                    args: [NGX_MONACO_EDITOR_CONFIG]
                }] }], propDecorators: { options: [{
                type: Input,
                args: ['options']
            }], originalModel: [{
                type: Input,
                args: ['originalModel']
            }], modifiedModel: [{
                type: Input,
                args: ['modifiedModel']
            }] } });
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZGlmZi1lZGl0b3IuY29tcG9uZW50LmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vLi4vcHJvamVjdHMvZWRpdG9yL3NyYy9saWIvZGlmZi1lZGl0b3IuY29tcG9uZW50LnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBLE9BQU8sRUFBRSxTQUFTLEVBQUUsTUFBTSxFQUFFLEtBQUssRUFBVSxNQUFNLGVBQWUsQ0FBQztBQUNqRSxPQUFPLEVBQUUsU0FBUyxFQUFFLE1BQU0sTUFBTSxDQUFDO0FBRWpDLE9BQU8sRUFBRSxVQUFVLEVBQUUsTUFBTSxlQUFlLENBQUM7QUFDM0MsT0FBTyxFQUFFLHdCQUF3QixFQUF5QixNQUFNLFVBQVUsQ0FBQzs7QUFvQjNFLE1BQU0sT0FBTyxtQkFBb0IsU0FBUSxVQUFVO0lBS2pELElBQ0ksT0FBTyxDQUFDLE9BQVk7UUFDdEIsSUFBSSxDQUFDLFFBQVEsR0FBRyxNQUFNLENBQUMsTUFBTSxDQUFDLEVBQUUsRUFBRSxJQUFJLENBQUMsTUFBTSxDQUFDLGNBQWMsRUFBRSxPQUFPLENBQUMsQ0FBQztRQUN2RSxJQUFJLElBQUksQ0FBQyxPQUFPLEVBQUU7WUFDaEIsSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLEVBQUUsQ0FBQztZQUN2QixJQUFJLENBQUMsVUFBVSxDQUFDLE9BQU8sRUFBRSxJQUFJLENBQUMsUUFBUSxDQUFDLENBQUM7U0FDekM7SUFDSCxDQUFDO0lBRUQsSUFBSSxPQUFPO1FBQ1QsT0FBTyxJQUFJLENBQUMsUUFBUSxDQUFDO0lBQ3ZCLENBQUM7SUFFRCxJQUNJLGFBQWEsQ0FBQyxLQUFzQjtRQUN0QyxJQUFJLENBQUMsY0FBYyxHQUFHLEtBQUssQ0FBQztRQUM1QixJQUFJLElBQUksQ0FBQyxPQUFPLEVBQUU7WUFDaEIsSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLEVBQUUsQ0FBQztZQUN2QixJQUFJLENBQUMsVUFBVSxDQUFDLElBQUksQ0FBQyxPQUFPLEVBQUUsSUFBSSxD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QixxTEFicEIsdURBQXVEOzsyRkFhdEQsbUJBQW1CO2tCQWYvQixTQUFTOytCQUNFLHdCQUF3QixZQUN4Qix1REFBdUQ7OzBCQWlEOUIsTUFBTTsyQkFBQyx3QkFBd0I7eUNBOUI5RCxPQUFPO3NCQURWLEtBQUs7dUJBQUMsU0FBUztnQkFjWixhQUFhO3NCQURoQixLQUFLO3VCQUFDLGVBQWU7Z0JBVWxCLGFBQWE7c0JBRGhCLEtBQUs7dUJBQUMsZUFBZSIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCB7IENvbXBvbmVudCwgSW5qZWN0LCBJbnB1dCwgTmdab25lIH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG5pbXBvcnQgeyBmcm9tRXZlbnQgfSBmcm9tICdyeGpzJztcblxuaW1wb3J0IHsgQmFzZUVkaXRvciB9IGZyb20gJy4vYmFzZS1lZGl0b3InO1xuaW1wb3J0IHsgTkdYX01PTkFDT19FRElUT1JfQ09ORklHLCBOZ3hNb25hY29FZGl0b3JDb25maWcgfSBmcm9tICcuL2NvbmZpZyc7XG5pbXBvcnQgeyBEaWZmRWRpdG9yTW9kZWwgfSBmcm9tICcuL3R5cGVzJztcblxuZGVjbGFyZSB2YXIgbW9uYWNvOiBhbnk7XG5cbkBDb21wb25lbnQoe1xuICBzZWxlY3RvcjogJ25neC1tb25hY28tZGlmZi1lZGl0b3InLFxuICB0ZW1wbGF0ZTogJzxkaXYgY2xhc3M9XCJlZGl0b3ItY29udGFpbmVyXCIgI2VkaXRvckNvbnRhaW5lcj48L2Rpdj4nLFxuICBzdHlsZXM6IFtgXG4gICAgOmhvc3Qge1xuICAgICAgZGlzcGxheTogYmxvY2s7XG4gICAgICBoZWlnaHQ6IDIwMHB4O1xuICAgIH1cblxuICAgIC5lZGl0b3ItY29udGFpbmVyIHtcbiAgICAgIHdpZHRoOiAxMDAlO1xuICAgICAgaGVpZ2h0OiA5OCU7XG4gICAgfVxuICBgXVxufSlcbmV4cG9ydCBjbGFzcyBEaWZmRWRpdG9yQ29tcG9uZW50IGV4dGVuZHMgQmFzZUVkaXRvciB7XG5cbiAgX29yaWdpbmFsTW9kZWw6IERpZmZFZGl0b3JNb2RlbDtcbiAgX21vZGlmaWVkTW9kZWw6IERpZmZFZGl0b3JNb2RlbDtcblxuICBASW5wdXQoJ29wdGlvbnMnKVxuICBzZXQgb3B0aW9ucyhvcHRpb25zOiBhbnkpIHtcbiAgICB0aGlzLl9vcHRpb25zID0gT2JqZWN0LmFzc2lnbih7fSwgdGhpcy5jb25maWcuZGVmYXVsdE9wdGlvbnMsIG9wdGlvbnMpO1xuICAgIGlmICh0aGlzLl9lZGl0b3IpIHtcbiAgICAgIHRoaXMuX2VkaXRvci5kaXNwb3NlKCk7XG4gICAgICB0aGlzLmluaXRNb25hY28ob3B0aW9ucywgdGhpcy5pbnNpZGVOZyk7XG4gICAgfVxuICB9XG5cbiAgZ2V0IG9wdGlvbnMoKTogYW55IHtcbiAgICByZXR1cm4gdGhpcy5fb3B0aW9ucztcbiAgfVxuXG4gIEBJbnB1dCgnb3JpZ2luYWxNb2RlbCcpXG4gIHNldCBvcmlnaW5hbE1vZGVsKG1vZGVsOiBEaWZmRWRpdG9yTW9kZWwpIHtcbiAgICB0aGlzLl9vcmlnaW5hbE1vZGVsID0gbW9kZWw7XG4gICAgaWYgKHRoaXMuX2VkaXRvcikge1xuICAgICAgdGhpcy5fZWRpdG9yLmRpc3Bvc2UoKTtcbiAgICAgIHRoaXMuaW5pdE1vbmFjbyh0aGlzLm9wdGlvbnMsIHRoaXMuaW5zaWRlTmcpO1xuICAgIH1cbiAgfVxuXG4gIEBJbnB1dCgnbW9kaWZpZWRNb2RlbCcpXG4gIHNldCBtb2RpZmllZE1vZGVsKG1vZGVsOiBEaWZmRWRpdG9yTW9kZWwpIHtcbiAgICB0aGlzLl9tb2RpZmllZE1vZGVsID0gbW9kZWw7XG4gICAgaWYgKHRoaXMuX2VkaXRvcikge1xuICAgICAgdGhpcy5fZWRpdG9yLmRpc3Bvc2UoKTtcbiAgICAgIHRoaXMuaW5pdE1vbmFjbyh0aGlzLm9wdGlvbnMsIHRoaXMuaW5zaWRlTmcpO1xuICAgIH1cbiAgfVxuXG4gIGNvbnN0cnVjdG9yKHByaXZhdGUgem9uZTogTmdab25lLCBASW5qZWN0KE5HWF9NT05BQ09fRURJVE9SX0NPTkZJRykgcHJpdmF0ZSBlZGl0b3JDb25maWc6IE5neE1vbmFjb0VkaXRvckNvbmZpZykge1xuICAgIHN1cGVyKGVkaXRvckNvbmZpZyk7XG4gIH1cblxuICBwcm90ZWN0ZWQgaW5pdE1vbmFjbyhvcHRpb25zOiBhbnksIGluc2lkZU5nOiBib29sZWFuKTogdm9pZCB7XG5cbiAgICBpZiAoIXRoaXMuX29yaWdpbmFsTW9kZWwgfHwgIXRoaXMuX21vZGlmaWVkTW9kZWwpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcignb3JpZ2luYWxNb2RlbCBvciBtb2RpZmllZE1vZGVsIG5vdCBmb3VuZCBmb3Igbmd4LW1vbmFjby1kaWZmLWVkaXRvcicpO1xuICAgIH1cblxuICAgIHRoaXMuX29yaWdpbmFsTW9kZWwubGFuZ3VhZ2UgPSB0aGlzLl9vcmlnaW5hbE1vZGVsLmxhbmd1YWdlIHx8IG9wdGlvbnMubGFuZ3VhZ2U7XG4gICAgdGhpcy5fbW9kaWZpZWRNb2RlbC5sYW5ndWFnZSA9IHRoaXMuX21vZGlmaWVkTW9kZWwubGFuZ3VhZ2UgfHwgb3B0aW9ucy5sYW5ndWFnZTtcblxuICAgIGxldCBvcmlnaW5hbE1vZGVsID0gbW9uYWNvLmVkaXRvci5jcmVhdGVNb2RlbCh0aGlzLl9vcmlnaW5hbE1vZGVsLmNvZGUsIHRoaXMuX29yaWdpbmFsTW9kZWwubGFuZ3VhZ2UpO1xuICAgIGxldCBtb2RpZmllZE1vZGVsID0gbW9uYWNvLmVkaXRvci5jcmVhdGVNb2RlbCh0aGlzLl9tb2RpZmllZE1vZGVsLmNvZGUsIHRoaXMuX21vZGlmaWVkTW9kZWwubGFuZ3VhZ2UpO1xuXG4gICAgdGhpcy5fZWRpdG9yQ29udGFpbmVyLm5hdGl2ZUVsZW1lbnQuaW5uZXJIVE1MID0gJyc7XG4gICAgY29uc3QgdGhlbWUgPSBvcHRpb25zLnRoZW1lO1xuXG4gICAgaWYgKGluc2lkZU5nKSB7XG4gICAgICB0aGlzLl9lZGl0b3IgPSBtb25hY28uZWRpdG9yLmNyZWF0ZURpZmZFZGl0b3IodGhpcy5fZWRpdG9yQ29udGFpbmVyLm5hdGl2ZUVsZW1lbnQsIG9wdGlvbnMpO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLnpvbmUucnVuT3V0c2lkZUFuZ3VsYXIoKCkgPT4ge1xuICAgICAgICB0aGlzLl9lZGl0b3IgPSBtb25hY28uZWRpdG9yLmNyZWF0ZURpZmZFZGl0b3IodGhpcy5fZWRpdG9yQ29udGFpbmVyLm5hdGl2ZUVsZW1lbnQsIG9wdGlvbnMpO1xuICAgICAgfSlcbiAgICB9XG5cbiAgICBvcHRpb25zLnRoZW1lID0gdGhlbWU7XG4gICAgdGhpcy5fZWRpdG9yLnNldE1vZGVsKHtcbiAgICAgIG9yaWdpbmFsOiBvcmlnaW5hbE1vZGVsLFxuICAgICAgbW9kaWZpZWQ6IG1vZGlmaWVkTW9kZWxcbiAgICB9KTtcblxuICAgIC8vIHJlZnJlc2ggbGF5b3V0IG9uIHJlc2l6ZSBldmVudC5cbiAgICBpZiAodGhpcy5fd2luZG93UmVzaXplU3Vic2NyaXB0aW9uKSB7XG4gICAgICB0aGlzLl93aW5kb3dSZXNpemVTdWJzY3JpcHRpb24udW5zdWJzY3JpYmUoKTtcbiAgICB9XG4gICAgdGhpcy5fd2luZG93UmVzaXplU3Vic2NyaXB0aW9uID0gZnJvbUV2ZW50KHdpbmRvdywgJ3Jlc2l6ZScpLnN1YnNjcmliZSgoKSA9PiB0aGlzLl9lZGl0b3IubGF5b3V0KCkpO1xuICAgIHRoaXMub25Jbml0LmVtaXQodGhpcy5fZWRpdG9yKTtcbiAgfVxuXG59XG4iXX0=