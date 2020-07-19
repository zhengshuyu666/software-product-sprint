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
                    console.log(res.commentList);
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
            var divWidth = this.getWidth(document.getElementById('chart-container'));
            google.charts.load('current', {'packages':['corechart']});
            google.charts.setOnLoadCallback(function() {
            fetch('/chart-data').then(response => response.json())
                .then((chart_data) => {
                    const data = new google.visualization.DataTable();
                    data.addColumn('string', 'Date');
                    data.addColumn('number', 'Comfirmed Cases');
                    Object.keys(chart_data).forEach((date) => {
                        data.addRow([date, chart_data[date]]);
                    });

                    const options = {
                        'title': 'Coronavirus Disease (COVID-2019) Globally Comfirmed Cases',
                        'width': divWidth,
                        'height': 400
                    };

                    const chart = new google.visualization.LineChart(
                        document.getElementById('chart-container'));
                    chart.draw(data, options);
                });
            });
        },
        /**
         * Compute element width
         */
        getStyle(el) {
            if (window.getComputedStyle)
                return window.getComputedStyle(el);
            return el.currentStyle;
        },
        getWidth(el) {
            var val = el.offsetWidth, which = ['left', 'right'];
            if (val == 0)           // display: none;
                return 0;
            var style = this.getStyle(el);
            for (var i = 0; i < which.length; i++) {
                val -= parseFloat(style["border-"+which[i]+"-width"]) || 0;
                val -= parseFloat(style["padding-"+which[i]]) || 0;
            }
            console.log("getWidth:", el, val);
            return val;
        },
        /** Creates a map and adds it to the page. */
        createMap () {
            var schoolLatLng = {lat: 39.9, lng: 116.3};
            var homeLatLng = {lat: 24.45, lng: 118.08};
            var myLatLng = {
                lat: (schoolLatLng.lat + homeLatLng.lat) / 2,
                lng: (schoolLatLng.lng + homeLatLng.lng) / 2
            };
            var map = new google.maps.Map(
                document.getElementById('map'), 
                {
                    zoom: 4,
                    center: myLatLng
                });

            var schoolMarker = new google.maps.Marker({
                position: schoolLatLng,
                map: map,
                title: 'Peking University'
            });
            var homeMarker = new google.maps.Marker({
                position: homeLatLng,
                map: map,
                title: 'My Hometown'
            });
        }
    },
    mounted () {
        // Load comment message once the document is ready
        this.getComments();
        this.initScroll();
        this.loadChart();
        this.createMap();
    }
});

