(function(document) {
    'use strict';

    // Grab a reference to our auto-binding template
    // and give it some initial binding values
    // Learn more about auto-binding templates at http://goo.gl/Dx1u2g
    var app = document.querySelector('#app');
    app.startTimeOfSimulation = null;
    // Sets app default base URL
    app.baseUrl = '/';
    if (window.location.port === '') {  // if production
        // Uncomment app.baseURL below and
        // set app.baseURL to '/your-pathname/' if running from folder in production
        // app.baseUrl = '/polymer-starter-kit/';
    }

    // Listen for template bound event to know when bindings
    // have resolved and content has been stamped to the page
    app.addEventListener('dom-change', function() {
        console.log('IRP Automation Adapter for the SBAC Open Source Test System is running');
    });

    // See https://github.com/Polymer/polymer/issues/1381
    window.addEventListener('WebComponentsReady', function() {
        // imports are loaded and elements have been registered

        var that = app;

        function irpAnalysisCallback(tdsReportLinks) {
            console.info( "Sending message to IRP's main window containing TDS Report URIs: " + JSON.stringify(tdsReportLinks) );
            window.parent.postMessage(tdsReportLinks, "*");
        }

        // Automation Mode events
        app.$.formAutomate.addEventListener('change', function (event) {
            // Validate the entire form to see if we should enable the `Submit` button.
            that.$.btnBeginAutomation.disabled = !that.$.formAutomate.validate();
        });
        app.$.formAutomate.addEventListener('iron-form-presubmit', function (event) {
            console.log("Presubmit: " + JSON.stringify(event));
            that.$.automationMessages.innerHTML = '<p>Starting IRP Automation...</p>';
            that.$.dlgAutomationStatus.open();
            that.$.automationProgressBar.disabled = false;
            that.$.automationProgressBar.hidden = false;
            that.$.btnAutomationProgressClose.hidden = true;
        });
        app.$.formAutomate.addEventListener('iron-form-error', function (event) {

            console.error(event.detail.response);

            that.$.automationMessages.innerHTML = '<p>Error starting automation</p>';

            that.$.automationProgressBar.disabled = true;
            that.$.automationProgressBar.hidden = true;
            that.$.btnAutomationProgressClose.hidden = false;
        });
        app.$.formAutomate.addEventListener('iron-form-response', function (event) {
            console.log("Automation request response: " + JSON.stringify(event.detail.response));

            var response = event.detail.response;

            if (response && response.errorMessage) {
                that.$.automationMessages.innerHTML = '<p>' + response.errorMessage + '</p>';
            } else {
                // Perform Long Polling
                var req = new Pollymer.Request({recurring: false, maxTries: 2});

                var statusLocation = event.detail.xhr.getResponseHeader("Location");

                var automationTicket = response;

                var pollStatus = function () {
                    var headers = {accept: 'application/json', 'content-type': 'application/json', "startTimeOfSimulation":that.startTimeOfSimulation};
                    req.start('GET', statusLocation, headers, null);
                }

                var getTdsReportLinks = function (location) {
                    var headers = {accept: 'application/json', 'content-type': 'application/json'};
                    req.start('GET', location, headers, null);
                }

                req.on('finished', function (code, response, headers) {

                    var continuePolling = false;
                    var automationTicket;
                    var automationStatusReport;

                    if (code == 303) {
                        getTdsReportLinks(headers['Location']);
                    }
                    else if (Array.isArray(response) && response.length > 0 && 'link' in response[0]) {
                        var links = [];
                        for (var i = 0; i < response.length; i++) {
                            links.push(response[i]["link"][0]["href"]);
                        }

                        irpAnalysisCallback(links);
                    }
                    else {
                        automationTicket = response;
                        if(that.startTimeOfSimulation==null ){
                        	that.startTimeOfSimulation =  automationTicket.startTimeOfSimulation;
                        }
                        automationStatusReport = automationTicket && automationTicket.adapterAutomationStatusReport;
                        if (automationStatusReport) {
                            continuePolling = !automationStatusReport.automationComplete && !automationStatusReport.error;

                            var phaseStatuses = automationStatusReport.phaseStatuses;

                            var messages = '';
                            for (var phase in phaseStatuses) {
                                messages += '<h3>' + phase + '</h3><ul>';
                                for (var i = 0; i < phaseStatuses[phase].length; i++) {
                                    messages += '<li>' + phaseStatuses[phase][i] + '</li>';
                                }
                                messages += '</ul>';
                            }

                            that.$.automationMessages.innerHTML = messages;
                            that.$.dlgAutomationStatus.notifyResize();
                        }
                    }

                    if (continuePolling) {
                        pollStatus(automationTicket);
                    } else {
                        that.$.automationProgressBar.disabled = true;
                        that.$.automationProgressBar.hidden = true;
                        that.$.btnAutomationProgressClose.hidden = false;
                        console.info('Automation done. Ending polling.');
                    }
                });

                req.on('error', function (reason) {
                    var message = reason == 'TransportError' ? 'Error connecting to IRP Server'
                                                             : 'Connection to IRP Server timed out';
                    that.$.automationMessages.innerHTML += '<p>Connection Error: ' + message + '</p>';
                });

                pollStatus(automationTicket);
            }
        });
    });
})(document);
