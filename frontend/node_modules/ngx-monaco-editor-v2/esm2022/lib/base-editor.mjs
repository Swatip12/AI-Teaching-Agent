import { Component, EventEmitter, Inject, Input, Output, ViewChild } from '@angular/core';
import { NGX_MONACO_EDITOR_CONFIG } from './config';
import * as i0 from "@angular/core";
let loadedMonaco = false;
let loadPromise;
export class BaseEditor {
    set insideNg(insideNg) {
        this._insideNg = insideNg;
        if (this._editor) {
            this._editor.dispose();
            this.initMonaco(this._options, this.insideNg);
        }
    }
    get insideNg() {
        return this._insideNg;
    }
    constructor(config) {
        this.config = config;
        this.onInit = new EventEmitter();
        this._insideNg = false;
    }
    ngAfterViewInit() {
        if (loadedMonaco) {
            // Wait until monaco editor is available
            loadPromise.then(() => {
                this.initMonaco(this._options, this.insideNg);
            });
        }
        else {
            loadedMonaco = true;
            loadPromise = new Promise((resolve) => {
                const baseUrl = this.config.baseUrl || "./assets";
                if (typeof (window.monaco) === 'object') {
                    this.initMonaco(this._options, this.insideNg);
                    resolve();
                    return;
                }
                const onGotAmdLoader = (require) => {
                    let usedRequire = require || window.require;
                    let requireConfig = { paths: { vs: `${baseUrl}/monaco/min/vs` } };
                    Object.assign(requireConfig, this.config.requireConfig || {});
                    // Load monaco
                    usedRequire.config(requireConfig);
                    usedRequire([`vs/editor/editor.main`], () => {
                        if (typeof this.config.onMonacoLoad === 'function') {
                            this.config.onMonacoLoad();
                        }
                        this.initMonaco(this._options, this.insideNg);
                        resolve();
                    });
                };
                if (this.config.monacoRequire) {
                    onGotAmdLoader(this.config.monacoRequire);
                    // Load AMD loader if necessary
                }
                else if (!window.require) {
                    const loaderScript = document.createElement('script');
                    loaderScript.type = 'text/javascript';
                    loaderScript.src = `${baseUrl}/monaco/min/vs/loader.js`;
                    loaderScript.addEventListener('load', () => { onGotAmdLoader(); });
                    document.body.appendChild(loaderScript);
                    // Load AMD loader without over-riding node's require
                }
                else if (!window.require.config) {
                    var src = `${baseUrl}/monaco/min/vs/loader.js`;
                    var loaderRequest = new XMLHttpRequest();
                    loaderRequest.addEventListener("load", () => {
                        let scriptElem = document.createElement('script');
                        scriptElem.type = 'text/javascript';
                        scriptElem.text = [
                            // Monaco uses a custom amd loader that over-rides node's require.
                            // Keep a reference to node's require so we can restore it after executing the amd loader file.
                            'var nodeRequire = require;',
                            loaderRequest.responseText.replace('"use strict";', ''),
                            // Save Monaco's amd require and restore Node's require
                            'var monacoAmdRequire = require;',
                            'require = nodeRequire;',
                            'require.nodeRequire = require;'
                        ].join('\n');
                        document.body.appendChild(scriptElem);
                        onGotAmdLoader(window.monacoAmdRequire);
                    });
                    loaderRequest.open("GET", src);
                    loaderRequest.send();
                }
                else {
                    onGotAmdLoader();
                }
            });
        }
    }
    ngOnDestroy() {
        if (this._windowResizeSubscription) {
            this._windowResizeSubscription.unsubscribe();
        }
        if (this._editor) {
            this._editor.dispose();
            this._editor = undefined;
        }
    }
    static { this.ɵfac = i0.ɵɵngDeclareFactory({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: BaseEditor, deps: [{ token: NGX_MONACO_EDITOR_CONFIG }], target: i0.ɵɵFactoryTarget.Component }); }
    static { this.ɵcmp = i0.ɵɵngDeclareComponent({ minVersion: "14.0.0", version: "17.0.3", type: BaseEditor, selector: "ng-component", inputs: { insideNg: "insideNg" }, outputs: { onInit: "onInit" }, viewQueries: [{ propertyName: "_editorContainer", first: true, predicate: ["editorContainer"], descendants: true, static: true }], ngImport: i0, template: '', isInline: true }); }
}
i0.ɵɵngDeclareClassMetadata({ minVersion: "12.0.0", version: "17.0.3", ngImport: i0, type: BaseEditor, decorators: [{
            type: Component,
            args: [{
                    template: ''
                }]
        }], ctorParameters: () => [{ type: undefined, decorators: [{
                    type: Inject,
                    args: [NGX_MONACO_EDITOR_CONFIG]
                }] }], propDecorators: { insideNg: [{
                type: Input,
                args: ['insideNg']
            }], _editorContainer: [{
                type: ViewChild,
                args: ['editorContainer', { static: true }]
            }], onInit: [{
                type: Output
            }] } });
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiYmFzZS1lZGl0b3IuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi9wcm9qZWN0cy9lZGl0b3Ivc3JjL2xpYi9iYXNlLWVkaXRvci50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQSxPQUFPLEVBRUwsU0FBUyxFQUVULFlBQVksRUFDWixNQUFNLEVBQ04sS0FBSyxFQUVMLE1BQU0sRUFDTixTQUFTLEVBQ1YsTUFBTSxlQUFlLENBQUM7QUFFdkIsT0FBTyxFQUFFLHdCQUF3QixFQUF5QixNQUFNLFVBQVUsQ0FBQzs7QUFFM0UsSUFBSSxZQUFZLEdBQUcsS0FBSyxDQUFDO0FBQ3pCLElBQUksV0FBMEIsQ0FBQztBQUsvQixNQUFNLE9BQWdCLFVBQVU7SUFFOUIsSUFDSSxRQUFRLENBQUMsUUFBaUI7UUFDNUIsSUFBSSxDQUFDLFNBQVMsR0FBRyxRQUFRLENBQUM7UUFDMUIsSUFBSSxJQUFJLENBQUMsT0FBTyxFQUFFO1lBQ2hCLElBQUksQ0FBQyxPQUFPLENBQUMsT0FBTyxFQUFFLENBQUM7WUFDdkIsSUFBSSxDQUFDLFVBQVUsQ0FBQyxJQUFJLENBQUMsUUFBUSxFQUFFLElBQUksQ0FBQyxRQUFRLENBQUMsQ0FBQztTQUMvQztJQUNILENBQUM7SUFFRCxJQUFJLFFBQVE7UUFDVixPQUFPLElBQUksQ0FBQyxTQUFTLENBQUM7SUFDeEIsQ0FBQztJQVNELFlBQXdELE1BQTZCO1FBQTdCLFdBQU0sR0FBTixNQUFNLENBQXVCO1FBTjNFLFdBQU0sR0FBRyxJQUFJLFlBQVksRUFBTyxDQUFDO1FBSW5DLGNBQVMsR0FBWSxLQUFLLENBQUM7SUFFcUQsQ0FBQztJQUV6RixlQUFlO1FBQ2IsSUFBSSxZQUFZLEVBQUU7WUFDaEIsd0NBQXdDO1lBQ3hDLFdBQVcsQ0FBQyxJQUFJLENBQUMsR0FBRyxFQUFFO2dCQUNwQixJQUFJLENBQUMsVUFBVSxDQUFDLElBQUksQ0FBQyxRQUFRLEVBQUUsSUFBSSxDQUFDLFFBQVEsQ0FBQyxDQUFDO1lB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IEVsZW1lbnRSZWYsXG4gIEV2ZW50RW1pdHRlcixcbiAgSW5qZWN0LFxuICBJbnB1dCxcbiAgT25EZXN0cm95LFxuICBPdXRwdXQsXG4gIFZpZXdDaGlsZFxufSBmcm9tICdAYW5ndWxhci9jb3JlJztcbmltcG9ydCB7IFN1YnNjcmlwdGlvbiB9IGZyb20gJ3J4anMnO1xuaW1wb3J0IHsgTkdYX01PTkFDT19FRElUT1JfQ09ORklHLCBOZ3hNb25hY29FZGl0b3JDb25maWcgfSBmcm9tICcuL2NvbmZpZyc7XG5cbmxldCBsb2FkZWRNb25hY28gPSBmYWxzZTtcbmxldCBsb2FkUHJvbWlzZTogUHJvbWlzZTx2b2lkPjtcblxuQENvbXBvbmVudCh7XG4gIHRlbXBsYXRlOiAnJ1xufSlcbmV4cG9ydCBhYnN0cmFjdCBjbGFzcyBCYXNlRWRpdG9yIGltcGxlbWVudHMgQWZ0ZXJWaWV3SW5pdCwgT25EZXN0cm95IHtcblxuICBASW5wdXQoJ2luc2lkZU5nJylcbiAgc2V0IGluc2lkZU5nKGluc2lkZU5nOiBib29sZWFuKSB7XG4gICAgdGhpcy5faW5zaWRlTmcgPSBpbnNpZGVOZztcbiAgICBpZiAodGhpcy5fZWRpdG9yKSB7XG4gICAgICB0aGlzLl9lZGl0b3IuZGlzcG9zZSgpO1xuICAgICAgdGhpcy5pbml0TW9uYWNvKHRoaXMuX29wdGlvbnMsIHRoaXMuaW5zaWRlTmcpO1xuICAgIH1cbiAgfVxuXG4gIGdldCBpbnNpZGVOZygpOiBib29sZWFuIHtcbiAgICByZXR1cm4gdGhpcy5faW5zaWRlTmc7XG4gIH1cblxuICBAVmlld0NoaWxkKCdlZGl0b3JDb250YWluZXInLCB7IHN0YXRpYzogdHJ1ZSB9KSBfZWRpdG9yQ29udGFpbmVyOiBFbGVtZW50UmVmO1xuICBAT3V0cHV0KCkgb25Jbml0ID0gbmV3IEV2ZW50RW1pdHRlcjxhbnk+KCk7XG4gIHByb3RlY3RlZCBfZWRpdG9yOiBhbnk7XG4gIHByb3RlY3RlZCBfb3B0aW9uczogYW55O1xuICBwcm90ZWN0ZWQgX3dpbmRvd1Jlc2l6ZVN1YnNjcmlwdGlvbjogU3Vic2NyaXB0aW9uO1xuICBwcml2YXRlIF9pbnNpZGVOZzogYm9vbGVhbiA9IGZhbHNlO1xuXG4gIGNvbnN0cnVjdG9yKEBJbmplY3QoTkdYX01PTkFDT19FRElUT1JfQ09ORklHKSBwcm90ZWN0ZWQgY29uZmlnOiBOZ3hNb25hY29FZGl0b3JDb25maWcpIHt9XG5cbiAgbmdBZnRlclZpZXdJbml0KCk6IHZvaWQge1xuICAgIGlmIChsb2FkZWRNb25hY28pIHtcbiAgICAgIC8vIFdhaXQgdW50aWwgbW9uYWNvIGVkaXRvciBpcyBhdmFpbGFibGVcbiAgICAgIGxvYWRQcm9taXNlLnRoZW4oKCkgPT4ge1xuICAgICAgICB0aGlzLmluaXRNb25hY28odGhpcy5fb3B0aW9ucywgdGhpcy5pbnNpZGVOZyk7XG4gICAgICB9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgbG9hZGVkTW9uYWNvID0gdHJ1ZTtcbiAgICAgIGxvYWRQcm9taXNlID0gbmV3IFByb21pc2U8dm9pZD4oKHJlc29sdmU6IGFueSkgPT4ge1xuICAgICAgICBjb25zdCBiYXNlVXJsID0gdGhpcy5jb25maWcuYmFzZVVybCB8fCBcIi4vYXNzZXRzXCI7XG4gICAgICAgIGlmICh0eXBlb2YgKCg8YW55PndpbmRvdykubW9uYWNvKSA9PT0gJ29iamVjdCcpIHtcbiAgICAgICAgICB0aGlzLmluaXRNb25hY28odGhpcy5fb3B0aW9ucywgdGhpcy5pbnNpZGVOZyk7XG4gICAgICAgICAgcmVzb2x2ZSgpO1xuICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuICAgICAgICBjb25zdCBvbkdvdEFtZExvYWRlcjogYW55ID0gKHJlcXVpcmU/OiBhbnkpID0+IHtcbiAgICAgICAgICBsZXQgdXNlZFJlcXVpcmUgPSByZXF1aXJlIHx8ICg8YW55PndpbmRvdykucmVxdWlyZTtcbiAgICAgICAgICBsZXQgcmVxdWlyZUNvbmZpZyA9IHsgcGF0aHM6IHsgdnM6IGAke2Jhc2VVcmx9L21vbmFjby9taW4vdnNgIH0gfTtcbiAgICAgICAgICBPYmplY3QuYXNzaWduKHJlcXVpcmVDb25maWcsIHRoaXMuY29uZmlnLnJlcXVpcmVDb25maWcgfHwge30pO1xuXG4gICAgICAgICAgLy8gTG9hZCBtb25hY29cbiAgICAgICAgICB1c2VkUmVxdWlyZS5jb25maWcocmVxdWlyZUNvbmZpZyk7XG4gICAgICAgICAgdXNlZFJlcXVpcmUoW2B2cy9lZGl0b3IvZWRpdG9yLm1haW5gXSwgKCkgPT4ge1xuICAgICAgICAgICAgaWYgKHR5cGVvZiB0aGlzLmNvbmZpZy5vbk1vbmFjb0xvYWQgPT09ICdmdW5jdGlvbicpIHtcbiAgICAgICAgICAgICAgdGhpcy5jb25maWcub25Nb25hY29Mb2FkKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICB0aGlzLmluaXRNb25hY28odGhpcy5fb3B0aW9ucywgdGhpcy5pbnNpZGVOZyk7XG4gICAgICAgICAgICByZXNvbHZlKCk7XG4gICAgICAgICAgfSk7XG4gICAgICAgIH07XG5cbiAgICAgICAgaWYgKHRoaXMuY29uZmlnLm1vbmFjb1JlcXVpcmUpIHtcbiAgICAgICAgICBvbkdvdEFtZExvYWRlcih0aGlzLmNvbmZpZy5tb25hY29SZXF1aXJlKTtcbiAgICAgICAgLy8gTG9hZCBBTUQgbG9hZGVyIGlmIG5lY2Vzc2FyeVxuICAgICAgICB9IGVsc2UgaWYgKCEoPGFueT53aW5kb3cpLnJlcXVpcmUpIHtcbiAgICAgICAgICBjb25zdCBsb2FkZXJTY3JpcHQ6IEhUTUxTY3JpcHRFbGVtZW50ID0gZG9jdW1lbnQuY3JlYXRlRWxlbWVudCgnc2NyaXB0Jyk7XG4gICAgICAgICAgbG9hZGVyU2NyaXB0LnR5cGUgPSAndGV4dC9qYXZhc2NyaXB0JztcbiAgICAgICAgICBsb2FkZXJTY3JpcHQuc3JjID0gYCR7YmFzZVVybH0vbW9uYWNvL21pbi92cy9sb2FkZXIuanNgO1xuICAgICAgICAgIGxvYWRlclNjcmlwdC5hZGRFdmVudExpc3RlbmVyKCdsb2FkJywgKCkgPT4geyBvbkdvdEFtZExvYWRlcigpOyB9KTtcbiAgICAgICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKGxvYWRlclNjcmlwdCk7XG4gICAgICAgIC8vIExvYWQgQU1EIGxvYWRlciB3aXRob3V0IG92ZXItcmlkaW5nIG5vZGUncyByZXF1aXJlXG4gICAgICAgIH0gZWxzZSBpZiAoISg8YW55PndpbmRvdykucmVxdWlyZS5jb25maWcpIHtcbiAgICAgICAgICAgIHZhciBzcmMgPSBgJHtiYXNlVXJsfS9tb25hY28vbWluL3ZzL2xvYWRlci5qc2A7XG4gICAgICAgICAgICBcbiAgICAgICAgICAgIHZhciBsb2FkZXJSZXF1ZXN0ID0gbmV3IFhNTEh0dHBSZXF1ZXN0KCk7XG4gICAgICAgICAgICBsb2FkZXJSZXF1ZXN0LmFkZEV2ZW50TGlzdGVuZXIoXCJsb2FkXCIsICgpID0+IHtcbiAgICAgICAgICAgICAgICBsZXQgc2NyaXB0RWxlbSA9IGRvY3VtZW50LmNyZWF0ZUVsZW1lbnQoJ3NjcmlwdCcpOyBcbiAgICAgICAgICAgICAgICBzY3JpcHRFbGVtLnR5cGUgPSAndGV4dC9qYXZhc2NyaXB0JztcbiAgICAgICAgICAgICAgICBzY3JpcHRFbGVtLnRleHQgPSBbXG4gICAgICAgICAgICAgICAgICAgIC8vIE1vbmFjbyB1c2VzIGEgY3VzdG9tIGFtZCBsb2FkZXIgdGhhdCBvdmVyLXJpZGVzIG5vZGUncyByZXF1aXJlLlxuICAgICAgICAgICAgICAgICAgICAvLyBLZWVwIGEgcmVmZXJlbmNlIHRvIG5vZGUncyByZXF1aXJlIHNvIHdlIGNhbiByZXN0b3JlIGl0IGFmdGVyIGV4ZWN1dGluZyB0aGUgYW1kIGxvYWRlciBmaWxlLlxuICAgICAgICAgICAgICAgICAgICAndmFyIG5vZGVSZXF1aXJlID0gcmVxdWlyZTsnLFxuICAgICAgICAgICAgICAgICAgICBsb2FkZXJSZXF1ZXN0LnJlc3BvbnNlVGV4dC5yZXBsYWNlKCdcInVzZSBzdHJpY3RcIjsnLCAnJyksXG4gICAgICAgICAgICAgICAgICAgIC8vIFNhdmUgTW9uYWNvJ3MgYW1kIHJlcXVpcmUgYW5kIHJlc3RvcmUgTm9kZSdzIHJlcXVpcmVcbiAgICAgICAgICAgICAgICAgICAgJ3ZhciBtb25hY29BbWRSZXF1aXJlID0gcmVxdWlyZTsnLFxuICAgICAgICAgICAgICAgICAgICAncmVxdWlyZSA9IG5vZGVSZXF1aXJlOycsXG4gICAgICAgICAgICAgICAgICAgICdyZXF1aXJlLm5vZGVSZXF1aXJlID0gcmVxdWlyZTsnXG4gICAgICAgICAgICAgICAgXS5qb2luKCdcXG4nKTtcbiAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKHNjcmlwdEVsZW0pO1xuICAgICAgICAgICAgICAgIG9uR290QW1kTG9hZGVyKCg8YW55PndpbmRvdykubW9uYWNvQW1kUmVxdWlyZSk7XG4gICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIGxvYWRlclJlcXVlc3Qub3BlbihcIkdFVFwiLCBzcmMpO1xuICAgICAgICAgICAgbG9hZGVyUmVxdWVzdC5zZW5kKCk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgb25Hb3RBbWRMb2FkZXIoKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfVxuICB9XG5cbiAgcHJvdGVjdGVkIGFic3RyYWN0IGluaXRNb25hY28ob3B0aW9uczogYW55LCBpbnNpZGVOZzogYm9vbGVhbik6IHZvaWQ7XG5cbiAgbmdPbkRlc3Ryb3koKSB7XG4gICAgaWYgKHRoaXMuX3dpbmRvd1Jlc2l6ZVN1YnNjcmlwdGlvbikge1xuICAgICAgdGhpcy5fd2luZG93UmVzaXplU3Vic2NyaXB0aW9uLnVuc3Vic2NyaWJlKCk7XG4gICAgfVxuICAgIGlmICh0aGlzLl9lZGl0b3IpIHtcbiAgICAgIHRoaXMuX2VkaXRvci5kaXNwb3NlKCk7XG4gICAgICB0aGlzLl9lZGl0b3IgPSB1bmRlZmluZWQ7XG4gICAgfVxuICB9XG59XG4iXX0=