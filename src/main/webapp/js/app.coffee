
# initialization logic
$ ->
  closeAlerts = -> 
    $(".alert").alert('close')

  setTimeout closeAlerts, 5000
