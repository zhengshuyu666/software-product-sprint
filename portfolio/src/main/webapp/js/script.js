// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var vm = new Vue({
    el: "#app",
    data: {
        isLogegIn: false,
        uploadURL: '',
        redirectURL: '',
        commentList: []
    },
    methods: {
        /**
         * Adds comment message to the page.
         */
        getComments () {
            console.log("getComments");
            var that = this;
            fetch('/data')
            .then(response => response.json())
            .then((res) => {
                that.isLogegIn = res.isLogegIn;
                that.redirectURL = res.redirectURL;
                that.uploadURL = res.uploadURL;
                if (res.isLogegIn) {
                    that.commentList = res.commentList;
                }
            });
        }, 
        /**
         * Add smooth scroll listeners
         */
        initScroll () {
            document.querySelector('#nav-home').addEventListener('click', function(e) {
                e.preventDefault();
                window.scroll({ top: 0, left: 0, behavior: 'smooth' });
            });
            document.querySelector('#nav-about').addEventListener('click', function(e) {
                e.preventDefault();
                document.querySelector('#about').scrollIntoView({ behavior: 'smooth' });
            });
            document.querySelector('#nav-publication').addEventListener('click', function(e) {
                e.preventDefault();
                document.querySelector('#publication').scrollIntoView({ behavior: 'smooth' });
            });
            document.querySelector('#nav-project').addEventListener('click', function(e) {
                e.preventDefault();
                document.querySelector('#project').scrollIntoView({ behavior: 'smooth' });
            });
            document.querySelector('#nav-hobby').addEventListener('click', function(e) {
                e.preventDefault();
                document.querySelector('#hobby').scrollIntoView({ behavior: 'smooth' });
            });
            document.querySelector('#nav-comment').addEventListener('click', function(e) {
                e.preventDefault();
                document.querySelector('#comment').scrollIntoView({ behavior: 'smooth' });
            });
            document.querySelector('#nav-back').addEventListener('click', function(e) {
                e.preventDefault();
                window.scroll({ top: 0, left: 0, behavior: 'smooth' });
            });
        },
        /**
         * Creates a chart and adds it to the page.
         */
        loadChart() {
            google.charts.load('current', {'packages':['corechart']});
            google.charts.setOnLoadCallback(function() {
                const data = new google.visualization.DataTable();
                data.addColumn('string', 'Animal');
                data.addColumn('number', 'Count');
                    data.addRows([
                      ['Lions', 10],
                      ['Tigers', 5],
                      ['Bears', 15]
                    ]);

                const options = {
                'title': 'Zoo Animals',
                'width':500,
                'height':400
                };

                const chart = new google.visualization.PieChart(
                  document.getElementById('chart-container'));
                chart.draw(data, options);
            });
        },

    },
    mounted () {
        // Load comment message once the document is ready
        this.getComments();
        this.initScroll();
        this.loadChart();
    }
});

