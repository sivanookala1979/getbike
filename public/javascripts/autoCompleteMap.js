var placeSearch, autocomplete;
        function initAutocomplete() {
        // Create the autocomplete object, restricting the search to geographical
        // location types.
        autocomplete = new google.maps.places.Autocomplete(
            (document.getElementById('autocomplete')),
            {types: ['geocode']});

        autocomplete2 = new google.maps.places.Autocomplete(
                    (document.getElementById('autocomplete2')),
                    {types: ['geocode']});
        // When the user selects an address from the dropdown, populate the address
        // fields in the form.
        autocomplete.addListener('place_changed', fillInAddress);
        autocomplete2.addListener('place_changed',fillInAddress);
      }

      function fillInAddress() {
        // Get the place details from the autocomplete object.
        var place = autocomplete.getPlace();

        //geting Lat Long
        var lat = autocomplete.getPlace().geometry.location.lat();
        var long = autocomplete.getPlace().geometry.location.lng();
        document.getElementById('lat').value =lat;
        document.getElementById('long').value =long;

        // Get each component of the address from the place details
        // and fill the corresponding field on the form.
        for (var i = 0; i < place.address_components.length; i++) {
          var addressType = place.address_components[i].types[0];
        }
      }
     function geolocate() {
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(function(position) {
            var geolocation = {
              lat: position.coords.latitude,
              lng: position.coords.longitude
            };
            var circle = new google.maps.Circle({
              center: geolocation,
              radius: position.coords.accuracy
            });
            autocomplete.setBounds(circle.getBounds());
          });
        }
      }