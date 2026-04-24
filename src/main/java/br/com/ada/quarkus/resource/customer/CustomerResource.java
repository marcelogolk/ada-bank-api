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
 * <p>Permite operações de:
 * <ul>
 *     <li>Cadastro de clientes (signup)</li>
 *     <li>Consulta de clientes</li>
 *     <li>Atualização de dados</li>
 * </ul>
 *
 * <p>Regras de segurança:
 * <ul>
 *     <li>GERENTE: acesso completo</li>
 *     <li>CLIENTE: acesso apenas aos próprios dados</li>
 * </ul>
 * </p>
 *
 * @author Marcelo
 * @version 2.0
 */
@Path("/clientes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    CustomerService customerService;

    @Inject
    CurrentUserService currentUserService;

    /**
     * Lista clientes com paginação.
     *
     * <p>Apenas gerentes podem acessar este endpoint.</p>
     *
     * @param page número da página (inicia em 0).
     * @param size quantidade de registros por página.
     * @return lista paginada de clientes.
     */
    @GET
    @RolesAllowed("GERENTE")
    public PageResponse<CustomerResponse> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        validatePagination(page, size);

        return PageResponse.from(
                customerService.list(page, size),
                this::toResponse
        );
    }

    /**
     * Busca um cliente pelo ID.
     *
     * <p>Gerentes podem acessar qualquer cliente.
     * Clientes só podem acessar a si mesmos.</p>
     *
     * @param id identificador do cliente.
     * @return dados públicos do cliente.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public CustomerResponse findById(@PathParam("id") Long id) {

        if (id == null) {
            throw new BadRequestException("O ID do cliente é obrigatório");
        }

        validateOwnershipOrManager(id);

        return toResponse(customerService.findById(id));
    }

    /**
     * Cria um novo cliente (cadastro).
     *
     * <p>Endpoint público que não requer autenticação.</p>
     *
     * @param request dados do cliente.
     * @param uriInfo informações da URI.
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
     * Atualiza os dados de um cliente.
     *
     * <p>Gerentes podem atualizar qualquer cliente.
     * Clientes só podem atualizar seus próprios dados.</p>
     *
     * @param id identificador do cliente.
     * @param request novos dados.
     * @return cliente atualizado.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public CustomerResponse update(
            @PathParam("id") Long id,
            @Valid UpdateCustomerRequest request) {

        if (id == null) {
            throw new BadRequestException("O ID do cliente é obrigatório");
        }

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
     * Valida se o usuário logado é o próprio cliente ou um gerente.
     *
     * @param customerId ID do cliente.
     */
    private void validateOwnershipOrManager(Long customerId) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        if (currentUser.isManager()) {
            return;
        }

        if (!currentUser.id().equals(customerId)) {
            throw new ForbiddenException(
                    "Acesso negado: apenas o próprio cliente ou um gerente pode realizar esta operação"
            );
        }
    }

    /**
     * Valida parâmetros de paginação.
     */
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page deve ser >= 0");
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size deve estar entre 1 e " + MAX_PAGE_SIZE);
        }
    }

    /**
     * Converte entidade Customer em DTO.
     */
    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }

    /**
     * Converte request em entidade Customer.
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