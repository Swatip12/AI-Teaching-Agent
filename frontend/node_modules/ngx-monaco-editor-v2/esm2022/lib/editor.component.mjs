import { Component, forwardRef, Inject, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { fromEvent } from 'rxjs';
import { BaseEditor } from './base-editor';
import { NGX_MONACO_EDITOR_CONFIG } from './config';
import * as i0 from "@angular/core";
export class EditorComponent extends BaseEditor {
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
    set model(model) {
        this.options.model = model;
        if (this._editor) {
            this._editor.dispose();
            this.initMonaco(this.options, this.insideNg);
        }
    }
    constructor(zone, editorConfig) {
        super(editorConfig);
        this.zone = zone;
        this.editorConfig = editorConfig;
        this._value = '';
        this.propagateChange = (_) => { };
        this.onTouched = () => { };
    }
    writeValue(value) {
        this._value = value || '';
        // Fix for value change while dispose in process.
        setTimeout(() => {
            if (this._editor && !this.options.model) {
                this._editor.setValue(this._value);
            }
        });
    }
    registerOnChange(fn) {
        this.propagateChange = fn;
    }
    registerOnTouched(fn) {
        this.onTouched = fn;
    }
    initMonaco(options, insideNg) {
        const hasModel = !!options.model;
        if (hasModel) {
            const model = monaco.editor.getModel(options.model.uri || '');
            if (model) {
                options.model = model;
                options.model.setValue(this._value);
            }
            else {
                options.model = monaco.editor.createModel(options.model.value, options.model.language, options.model.uri);
            }
        }
        if (insideNg) {
            this._editor = monaco.editor.create(this._editorContainer.nativeElement, options);
        }
        else {
            this.zone.runOutsideAngular(() => {
                this._editor = monaco.editor.create(this._editorContainer.nativeElement, options);
            });
        }
        if (!hasModel) {
            this._editor.setValue(this._value);
        }
        this._editor.onDidChangeModelContent((e) => {
            const value = this._editor.getValue();
            // value is not propagated to parent when executing outside zone.
            this.zone.run(() => {
                this.propagateChange(value);
                this._value = value;
            });
        });
        this._editor.onDidBlurEditorWidget(() => {
            this.onTouched();
        });
        // refresh layout on resize event.
        if (this._windowResizeSubscription) {
            this._windowResizeSubscription.unsubscribe();
        }
        this._windowResizeSubscription = fromEvent(window, 'resize').subscribe(() => this._editor.layout());
        this.onInit.emit(this._editor);
    }
    static { this.ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: EditorComponent, deps: [{ token: i0.NgZone }, { token: NGX_MONACO_EDITOR_CONFIG }], target: i0.ɵɵFactoryTarget.Component }); }
    static { this.ɵcmp = i0.ɵɵngDeclareComponent({ minVersion: "14.0.0", version: "17.0.3", type: EditorComponent, selector: "ngx-monaco-editor", inputs: { options: "options", model: "model" }, providers: [{
                provide: NG_VALUE_ACCESSOR,
                useExisting: forwardRef(() => EditorComponent),
                multi: true
            }], usesInheritance: true, ngImport: i0, template: '<div class="editor-container" #editorContainer></div>', isInline: true, styles: [":host{display:block;height:200px}.editor-container{width:100%;height:98%}\n"] }); }
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: EditorComponent, decorators: [{
            type: Component,
            args: [{ selector: 'ngx-monaco-editor', template: '<div class="editor-container" #editorContainer></div>', providers: [{
                            provide: NG_VALUE_ACCESSOR,
                            useExisting: forwardRef(() => EditorComponent),
                            multi: true
                        }], styles: [":host{display:block;height:200px}.editor-container{width:100%;height:98%}\n"] }]
        }], ctorParameters: () => [{ type: i0.NgZone }, { type: undefined, decorators: [{
                    type: Inject,
                    args: [NGX_MONACO_EDITOR_CONFIG]
                }] }], propDecorators: { options: [{
                type: Input,
                args: ['options']
            }], model: [{
                type: Input,
                args: ['model']
            }] } });
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZWRpdG9yLmNvbXBvbmVudC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uL3Byb2plY3RzL2VkaXRvci9zcmMvbGliL2VkaXRvci5jb21wb25lbnQudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUEsT0FBTyxFQUFFLFNBQVMsRUFBRSxVQUFVLEVBQUUsTUFBTSxFQUFFLEtBQUssRUFBVSxNQUFNLGVBQWUsQ0FBQztBQUM3RSxPQUFPLEVBQXdCLGlCQUFpQixFQUFFLE1BQU0sZ0JBQWdCLENBQUM7QUFDekUsT0FBTyxFQUFFLFNBQVMsRUFBRSxNQUFNLE1BQU0sQ0FBQztBQUVqQyxPQUFPLEVBQUUsVUFBVSxFQUFFLE1BQU0sZUFBZSxDQUFDO0FBQzNDLE9BQU8sRUFBRSx3QkFBd0IsRUFBeUIsTUFBTSxVQUFVLENBQUM7O0FBeUIzRSxNQUFNLE9BQU8sZUFBZ0IsU0FBUSxVQUFVO0lBTTdDLElBQ0ksT0FBTyxDQUFDLE9BQVk7UUFDdEIsSUFBSSxDQUFDLFFBQVEsR0FBRyxNQUFNLENBQUMsTUFBTSxDQUFDLEVBQUUsRUFBRSxJQUFJLENBQUMsTUFBTSxDQUFDLGNBQWMsRUFBRSxPQUFPLENBQUMsQ0FBQztRQUN2RSxJQUFJLElBQUksQ0FBQyxPQUFPLEVBQUU7WUFDaEIsSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLEVBQUUsQ0FBQztZQUN2QixJQUFJLENBQUMsVUFBVSxDQUFDLE9BQU8sRUFBRSxJQUFJLENBQUMsUUFBUSxDQUFDLENBQUM7U0FDekM7SUFDSCxDQUFDO0lBRUQsSUFBSSxPQUFPO1FBQ1QsT0FBTyxJQUFJLENBQUMsUUFBUSxDQUFDO0lBQ3ZCLENBQUM7SUFFRCxJQUNJLEtBQUssQ0FBQyxLQUFxQjtRQUM3QixJQUFJLENBQUMsT0FBTyxDQUFDLEtBQUssR0FBRyxLQUFLLENBQUM7UUFDM0IsSUFBSSxJQUFJLENBQUMsT0FBTyxFQUFFO1lBQ2hCLElBQUksQ0FBQyxPQUFPLENBQUMsT0FBTyxFQUFFLENB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RFYsS0FBSzt1QkFBQyxTQUFTO2dCQWNaLEtBQUs7c0JBRFIsS0FBSzt1QkFBQyxPQUFPIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IHsgQ29tcG9uZW50LCBmb3J3YXJkUmVmLCBJbmplY3QsIElucHV0LCBOZ1pvbmUgfSBmcm9tICdAYW5ndWxhci9jb3JlJztcbmltcG9ydCB7IENvbnRyb2xWYWx1ZUFjY2Vzc29yLCBOR19WQUxVRV9BQ0NFU1NPUiB9IGZyb20gJ0Bhbmd1bGFyL2Zvcm1zJztcbmltcG9ydCB7IGZyb21FdmVudCB9IGZyb20gJ3J4anMnO1xuXG5pbXBvcnQgeyBCYXNlRWRpdG9yIH0gZnJvbSAnLi9iYXNlLWVkaXRvcic7XG5pbXBvcnQgeyBOR1hfTU9OQUNPX0VESVRPUl9DT05GSUcsIE5neE1vbmFjb0VkaXRvckNvbmZpZyB9IGZyb20gJy4vY29uZmlnJztcbmltcG9ydCB7IE5neEVkaXRvck1vZGVsIH0gZnJvbSAnLi90eXBlcyc7XG5cbmRlY2xhcmUgdmFyIG1vbmFjbzogYW55O1xuXG5AQ29tcG9uZW50KHtcbiAgc2VsZWN0b3I6ICduZ3gtbW9uYWNvLWVkaXRvcicsXG4gIHRlbXBsYXRlOiAnPGRpdiBjbGFzcz1cImVkaXRvci1jb250YWluZXJcIiAjZWRpdG9yQ29udGFpbmVyPjwvZGl2PicsXG4gIHN0eWxlczogW2BcbiAgICAgIDpob3N0IHtcbiAgICAgICAgICBkaXNwbGF5OiBibG9jaztcbiAgICAgICAgICBoZWlnaHQ6IDIwMHB4O1xuICAgICAgfVxuXG4gICAgICAuZWRpdG9yLWNvbnRhaW5lciB7XG4gICAgICAgICAgd2lkdGg6IDEwMCU7XG4gICAgICAgICAgaGVpZ2h0OiA5OCU7XG4gICAgICB9XG4gIGBdLFxuICBwcm92aWRlcnM6IFt7XG4gICAgcHJvdmlkZTogTkdfVkFMVUVfQUNDRVNTT1IsXG4gICAgdXNlRXhpc3Rpbmc6IGZvcndhcmRSZWYoKCkgPT4gRWRpdG9yQ29tcG9uZW50KSxcbiAgICBtdWx0aTogdHJ1ZVxuICB9XVxufSlcbmV4cG9ydCBjbGFzcyBFZGl0b3JDb21wb25lbnQgZXh0ZW5kcyBCYXNlRWRpdG9yIGltcGxlbWVudHMgQ29udHJvbFZhbHVlQWNjZXNzb3Ige1xuICBwcml2YXRlIF92YWx1ZTogc3RyaW5nID0gJyc7XG5cbiAgcHJvcGFnYXRlQ2hhbmdlID0gKF86IGFueSkgPT4ge307XG4gIG9uVG91Y2hlZCA9ICgpID0+IHt9O1xuXG4gIEBJbnB1dCgnb3B0aW9ucycpXG4gIHNldCBvcHRpb25zKG9wdGlvbnM6IGFueSkge1xuICAgIHRoaXMuX29wdGlvbnMgPSBPYmplY3QuYXNzaWduKHt9LCB0aGlzLmNvbmZpZy5kZWZhdWx0T3B0aW9ucywgb3B0aW9ucyk7XG4gICAgaWYgKHRoaXMuX2VkaXRvcikge1xuICAgICAgdGhpcy5fZWRpdG9yLmRpc3Bvc2UoKTtcbiAgICAgIHRoaXMuaW5pdE1vbmFjbyhvcHRpb25zLCB0aGlzLmluc2lkZU5nKTtcbiAgICB9XG4gIH1cblxuICBnZXQgb3B0aW9ucygpOiBhbnkge1xuICAgIHJldHVybiB0aGlzLl9vcHRpb25zO1xuICB9XG5cbiAgQElucHV0KCdtb2RlbCcpXG4gIHNldCBtb2RlbChtb2RlbDogTmd4RWRpdG9yTW9kZWwpIHtcbiAgICB0aGlzLm9wdGlvbnMubW9kZWwgPSBtb2RlbDtcbiAgICBpZiAodGhpcy5fZWRpdG9yKSB7XG4gICAgICB0aGlzLl9lZGl0b3IuZGlzcG9zZSgpO1xuICAgICAgdGhpcy5pbml0TW9uYWNvKHRoaXMub3B0aW9ucywgdGhpcy5pbnNpZGVOZyk7XG4gICAgfVxuICB9XG5cbiAgY29uc3RydWN0b3IocHJpdmF0ZSB6b25lOiBOZ1pvbmUsIEBJbmplY3QoTkdYX01PTkFDT19FRElUT1JfQ09ORklHKSBwcml2YXRlIGVkaXRvckNvbmZpZzogTmd4TW9uYWNvRWRpdG9yQ29uZmlnKSB7XG4gICAgc3VwZXIoZWRpdG9yQ29uZmlnKTtcbiAgfVxuXG4gIHdyaXRlVmFsdWUodmFsdWU6IGFueSk6IHZvaWQge1xuICAgIHRoaXMuX3ZhbHVlID0gdmFsdWUgfHwgJyc7XG4gICAgLy8gRml4IGZvciB2YWx1ZSBjaGFuZ2Ugd2hpbGUgZGlzcG9zZSBpbiBwcm9jZXNzLlxuICAgIHNldFRpbWVvdXQoKCkgPT4ge1xuICAgICAgaWYgKHRoaXMuX2VkaXRvciAmJiAhdGhpcy5vcHRpb25zLm1vZGVsKSB7XG4gICAgICAgIHRoaXMuX2VkaXRvci5zZXRWYWx1ZSh0aGlzLl92YWx1ZSk7XG4gICAgICB9XG4gICAgfSk7XG4gIH1cblxuICByZWdpc3Rlck9uQ2hhbmdlKGZuOiBhbnkpOiB2b2lkIHtcbiAgICB0aGlzLnByb3BhZ2F0ZUNoYW5nZSA9IGZuO1xuICB9XG5cbiAgcmVnaXN0ZXJPblRvdWNoZWQoZm46IGFueSk6IHZvaWQge1xuICAgIHRoaXMub25Ub3VjaGVkID0gZm47XG4gIH1cblxuICBwcm90ZWN0ZWQgaW5pdE1vbmFjbyhvcHRpb25zOiBhbnksIGluc2lkZU5nOiBib29sZWFuKTogdm9pZCB7XG5cbiAgICBjb25zdCBoYXNNb2RlbCA9ICEhb3B0aW9ucy5tb2RlbDtcblxuICAgIGlmIChoYXNNb2RlbCkge1xuICAgICAgY29uc3QgbW9kZWwgPSBtb25hY28uZWRpdG9yLmdldE1vZGVsKG9wdGlvbnMubW9kZWwudXJpIHx8ICcnKTtcbiAgICAgIGlmIChtb2RlbCkge1xuICAgICAgICBvcHRpb25zLm1vZGVsID0gbW9kZWw7XG4gICAgICAgIG9wdGlvbnMubW9kZWwuc2V0VmFsdWUodGhpcy5fdmFsdWUpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgb3B0aW9ucy5tb2RlbCA9IG1vbmFjby5lZGl0b3IuY3JlYXRlTW9kZWwob3B0aW9ucy5tb2RlbC52YWx1ZSwgb3B0aW9ucy5tb2RlbC5sYW5ndWFnZSwgb3B0aW9ucy5tb2RlbC51cmkpO1xuICAgICAgfVxuICAgIH1cblxuICAgIGlmIChpbnNpZGVOZykge1xuICAgICAgdGhpcy5fZWRpdG9yID0gbW9uYWNvLmVkaXRvci5jcmVhdGUodGhpcy5fZWRpdG9yQ29udGFpbmVyLm5hdGl2ZUVsZW1lbnQsIG9wdGlvbnMpO1xuICAgIH0gZWxzZSB7XG4gICAgICB0aGlzLnpvbmUucnVuT3V0c2lkZUFuZ3VsYXIoKCkgPT4ge1xuICAgICAgICB0aGlzLl9lZGl0b3IgPSBtb25hY28uZWRpdG9yLmNyZWF0ZSh0aGlzLl9lZGl0b3JDb250YWluZXIubmF0aXZlRWxlbWVudCwgb3B0aW9ucyk7XG4gICAgICB9KVxuICAgIH1cblxuICAgIGlmICghaGFzTW9kZWwpIHtcbiAgICAgIHRoaXMuX2VkaXRvci5zZXRWYWx1ZSh0aGlzLl92YWx1ZSk7XG4gICAgfVxuXG4gICAgdGhpcy5fZWRpdG9yLm9uRGlkQ2hhbmdlTW9kZWxDb250ZW50KChlOiBhbnkpID0+IHtcbiAgICAgIGNvbnN0IHZhbHVlID0gdGhpcy5fZWRpdG9yLmdldFZhbHVlKCk7XG5cbiAgICAgIC8vIHZhbHVlIGlzIG5vdCBwcm9wYWdhdGVkIHRvIHBhcmVudCB3aGVuIGV4ZWN1dGluZyBvdXRzaWRlIHpvbmUuXG4gICAgICB0aGlzLnpvbmUucnVuKCgpID0+IHtcbiAgICAgICAgdGhpcy5wcm9wYWdhdGVDaGFuZ2UodmFsdWUpO1xuICAgICAgICB0aGlzLl92YWx1ZSA9IHZhbHVlO1xuICAgICAgfSk7XG4gICAgfSk7XG5cbiAgICB0aGlzLl9lZGl0b3Iub25EaWRCbHVyRWRpdG9yV2lkZ2V0KCgpID0+IHtcbiAgICAgIHRoaXMub25Ub3VjaGVkKCk7XG4gICAgfSk7XG5cbiAgICAvLyByZWZyZXNoIGxheW91dCBvbiByZXNpemUgZXZlbnQuXG4gICAgaWYgKHRoaXMuX3dpbmRvd1Jlc2l6ZVN1YnNjcmlwdGlvbikge1xuICAgICAgdGhpcy5fd2luZG93UmVzaXplU3Vic2NyaXB0aW9uLnVuc3Vic2NyaWJlKCk7XG4gICAgfVxuICAgIHRoaXMuX3dpbmRvd1Jlc2l6ZVN1YnNjcmlwdGlvbiA9IGZyb21FdmVudCh3aW5kb3csICdyZXNpemUnKS5zdWJzY3JpYmUoKCkgPT4gdGhpcy5fZWRpdG9yLmxheW91dCgpKTtcbiAgICB0aGlzLm9uSW5pdC5lbWl0KHRoaXMuX2VkaXRvcik7XG4gIH1cblxufVxuIl19