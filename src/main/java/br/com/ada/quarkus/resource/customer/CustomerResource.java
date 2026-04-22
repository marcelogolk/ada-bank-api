package br.com.ada.quarkus.resource.customer;

import br.com.ada.quarkus.model.Customer;
import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.resource.PageResponse;
import br.com.ada.quarkus.service.CurrentUserService;
import br.com.ada.quarkus.service.CustomerService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/**
 * Recurso responsável pelos endpoints de clientes.
 *
 * <p>Recebe as requisições HTTP relacionadas ao cadastro,
 * consulta e atualização de clientes com autenticação e paginação.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@Path("/clientes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @Inject
    CurrentUserService currentUserService;

    /**
     * Lista todos os clientes cadastrados com paginação.
     *
     * <p>Endpoint público que retorna uma página de clientes
     * ordenados por ID.</p>
     *
     * @param page Número da página (0-indexed). Padrão: 0
     * @param size Quantidade de clientes por página. Padrão: 10
     * @return página de clientes com dados públicos.
     */
    @GET
    @RolesAllowed("GERENTE")
    public PageResponse<CustomerResponse> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return PageResponse.from(
                customerService.list(page, size),
                this::toResponse
        );
    }

    /**
     * Busca um cliente pelo id.
     *
     * <p>Endpoint público que retorna os dados públicos
     * de um cliente específico.</p>
     *
     * @param id identificador do cliente.
     * @return dados públicos do cliente encontrado.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"GERENTE","CLIENTE"})
    public CustomerResponse findById(@PathParam("id") Long id) {

        validateOwnershipOrManager(id);

        return toResponse(customerService.findById(id));
    }

    /**
     * Cadastra um novo cliente (signup).
     *
     * <p>Endpoint público que permite o cadastro de novos clientes
     * sem autenticação prévia.</p>
     *
     * @param request dados do cliente a ser criado.
     * @param uriInfo informações da URI da requisição.
     * @return resposta HTTP 201 com o cliente criado.
     */
    @POST
    @Transactional
    @PermitAll
    public Response create(
            @Valid CreateCustomerRequest request,
            @Context UriInfo uriInfo) {
        Customer customer = customerService.create(toCustomer(request));
        CustomerResponse response = toResponse(customer);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(response.id().toString())
                .build();

        return Response.created(location)
                .entity(response)
                .build();
    }

    /**
     * Atualiza os dados permitidos de um cliente.
     *
     * <p>Apenas o próprio cliente ou um gerente podem atualizar
     * os dados de um cliente. O CPF não pode ser alterado.</p>
     *
     * @param id identificador do cliente a atualizar.
     * @param request novos dados do cliente.
     * @return dados públicos do cliente atualizado.
     * @throws ForbiddenException quando o usuário não tem permissão.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public CustomerResponse update(
            @PathParam("id") Long id,
            @Valid UpdateCustomerRequest request) {

        // Validar propriedade: apenas o próprio cliente ou gerente pode atualizar
        validateOwnershipOrManager(id);

        Customer updatedCustomer = customerService.update(
                id,
                request.name(),
                request.email(),
                request.password()
        );

        return toResponse(updatedCustomer);
    }

    /**
     * Valida se o usuário logado é o proprietário da conta ou é gerente.
     *
     * <p>Gerentes podem atualizar qualquer cliente.
     * Clientes só podem atualizar a si mesmos.</p>
     *
     * @param customerId ID do cliente a ser atualizado.
     * @throws ForbiddenException quando o usuário não tem permissão.
     */
    private void validateOwnershipOrManager(Long customerId) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        // Gerente pode fazer qualquer coisa
        if (currentUser.isManager()) {
            return;
        }

        // Cliente só pode atualizar a si mesmo
        if (!currentUser.id().equals(customerId)) {
            throw new ForbiddenException(
                    "Acesso negado: apenas o proprietário da conta ou um gerente pode realizar esta operação"
            );
        }
    }

    /**
     * Converte a entidade Customer em CustomerResponse.
     *
     * @param customer entidade de cliente.
     * @return resposta com dados públicos do cliente.
     */
    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }

    /**
     * Converte o request de criação em entidade Customer.
     *
     * @param request dados recebidos no cadastro.
     * @return entidade Customer.
     */
    private Customer toCustomer(CreateCustomerRequest request) {
        return new Customer(
                null,
                request.name(),
                request.cpf(),
                request.email(),
                request.password()
        );
    }
}