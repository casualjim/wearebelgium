

# initialization logic
$ ->
  closeAlerts = -> 
    $(".alert").alert('close')

  setTimeout closeAlerts, 5000
  $('.book-week').on 'click', ->
    alert("data id: " + $(@).attr("data-id"))