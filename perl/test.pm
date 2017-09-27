#!/usr/bin/perl
push (@INC, 'pwd');

use VTWebservice;
use strict; use warnings;
use Data::Dumper;

my $wsObj = new VTWebservice('http://localhost/crm650/', 'admin', 'UFPaI0vZxOQMImV8');

$wsObj->login();
my %contact = ('firstname' => 'pinaki', 'lastname' => 'das', 'email' => 'pinaki17@gmail.com');
$wsObj->create(\%contact, 'Contacts');

my %result1 = $wsObj->query("select * from Contacts where lastname='test-pinaki';");
print Dumper(\%result1);
