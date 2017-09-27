#!/usr/bin/perl
#perl client for vtiger webservices
#requires the REST::Client and JSON pacakges
package VTWebservice;

use REST::Client;
use JSON;
use Digest::MD5 qw(md5_hex);
use Data::Dumper;

require Exporter;

@ISA = qw(Exporter);
@EXPORT = qw(login, query, logout);

#constructor. gets the url, username, and accesskey and initializes them as class variables
sub new {
    my $type = shift;
    my ($url, $username, $accessKey) = @_;
    my $this = {};

    $this->{'url'} = $url;
    $this->{'username'} = $username;
    $this->{'accessKey'} = $accessKey;

    bless $this, $type;
}

#login to webservices
sub login {
    my $this = shift;
    my %params = ('operation' => 'getchallenge', 'username' => $this->{'username'});

    my $result = $this->doGet(\%params);
    my $token = %$result{'token'};

    my $encodedKey = md5_hex($token.$this->{'accessKey'});
    my %postParams = ('operation' => 'login', 'username' => $this->{'username'}, 'accessKey' => $encodedKey);
    my $loginResult = $this->doPost(\%postParams);

    my $sessionName = %$loginResult{"sessionName"};
    $this->{'sessionName'} = $sessionName;
    $this->{'userId'} = %$loginResult{"userId"};
}

#generic post method. takes a list of parameters and post to the url set in constructor
sub doPost {
    my $this = shift;
    my $paramsRef = shift;

    my $params = "";
    while (my($name, $value) = each %{$paramsRef}) {
        $params .= $name . "=" . $value . "&";
    }

    my $client = REST::Client->new();
    $client->setHost($this->{'url'});
    $client->POST("webservice.php", $params, {'Content-type' => 'application/x-www-form-urlencoded'});

    my $response;
    eval {
        $response = from_json($client->responseContent());
    } or do {
        print "Invalid json response \n\n";
        print $client->responseContent();
        die;
    };

    if(%$response{'success'} == 1) {
        return %$response{'result'};
    } else {
        print "Operation ". %$paramsRef{'operation'} ." failed!\n\n";
        return %$response;
    }
}

#generic get method
sub doGet {
    my $this = shift;
    my $paramsRef = shift;

    my $params = "webservice.php?";
    while (my($name, $value) = each %{$paramsRef}) {
        $params .= $name . "=" . $value . "&";
    }

    my $client = REST::Client->new();
    $client->setHost($this->{'url'});
    $client->GET($params);

    my $response;
    eval {
        $response = from_json($client->responseContent());
    } or do {
        print "Invalid json response \n\n";
        print $client->responseContent();
        die;
    };

    if(%$response{'success'} == 1) {
        return %$response{'result'};
    } else {
        print "Operation ". %$paramsRef{'operation'} ." failed!\n\n";
        return %$response;
    }
}

#get wrapper subroutine, takes operation name and parameters and prepares the doGet call
sub get {
    my $this = shift;
    my $operation = shift;
    my $params = shift;

    my %prepared = ('operation' => $operation, 'sessionName' => $this->{'sessionName'}, %$params);
    return $this->doGet(\%prepared);
}

#post wrapper subroutine, takes operation name and parameters and prepares the doPost call
sub post {
    my $this = shift;
    my $operation = shift;
    my $params = shift;

    my %prepared = ('operation' => $operation, 'sessionName' => $this->{'sessionName'}, %$params);
    return $this->doPost(\%prepared);
}

#webservice query call, takes a valid webservice query and returns the resultset as array of hashes
sub query {
    my $this = shift;
    my $query = shift;

    my %params = ('query' => $query);
    return $this->get('query', \%params);
}

#creates an entity. accepts the object hash and object type. creates the post call parameters and returns the created entity
sub create {
    my $this = shift;
    my $object = shift;
    my $objectType = shift;

    my %element = %$object;
    if($object{'assigned_user_id'} == "") {
        %element = ('assigned_user_id' => $this->{"userId"}, %$object);
    }
    my $objectJson = encode_json(\%element);
    my %prepared = ('element' => $objectJson, 'elementType' => $objectType);
    return $this->post('create', \%prepared);
}

#update entity
sub update {
    my $this = shift;
    my $object = shift;

    my $objectJson = encode_json(\%object);
    my %prepared = ('element' => $objectJson);
    return $this->post('update', \%prepared);
}

sub revise {
    my $this = shift;
    my $object = shift;

    my $objectJson = encode_json(\%object);
    my %prepared = ('element' => $objectJson);
    return $this->post('revise', \%prepared);
}

sub listTypes {
    my $this = shift;
    my %params = ();
    return $this->get('listtypes', \%params);
}

sub describe {
    my $this = shift;
    my $objectName = shift;

    my %prepared = ('element' => $objectName);
    return $this->get('describe', \%prepared);
}

sub retrieve {
    my $this = shift;
    my $entityId = shift;

    my %prepared = ('id' => $entityId);
    return $this->get('retrieve', \%prepared);
}

sub delete {
    my $this = shift;
    my $entityId = shift;

    my %prepared = ('id' => $entityId);
    return $this->post('delete', \%prepared);
}



#destructor to handle cleanup
sub DESTROY {
    my $this = shift;
    $this->logout();
}

#logout from webservices
sub logout {
    my $this = shift;
    my %params = ();
    $this->post('logout', \%params);
}

#debugging function
sub print_r {
    my $arr = shift;
    print "\n--------\n";
    print Dumper(\$arr);
    print "--------\n";
}

1;
